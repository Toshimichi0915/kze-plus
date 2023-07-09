package net.toshimichi.kzeplus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleEventRegistry implements EventRegistry {

    private final Map<Class<?>, List<EventHandler<?>>> handlers = new HashMap<>();
    private final Map<Object, List<EventHandler<?>>> objects = new HashMap<>();

    @Override
    public void register(EventHandler<?> handler) {
        handlers.computeIfAbsent(handler.getEventClass(), k -> new ArrayList<>()).add(handler);
    }

    @Override
    public void unregister(EventHandler<?> handler) {
        List<EventHandler<?>> handlers = this.handlers.get(handler.getEventClass());
        if (handlers == null) return;
        handlers.remove(handler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void call(Event event) {
        List<EventHandler<?>> handlers = this.handlers.get(event.getClass());
        if (handlers == null) return;
        for (EventHandler<?> handler : List.copyOf(handlers)) {
            try {
                ((EventHandler<Event>) handler).handle(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(Object obj) {

        List<EventHandler<?>> handlers = new ArrayList<>();

        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() != 1) continue;
            if (method.getAnnotation(EventTarget.class) == null) continue;

            Class<?> eventClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventClass)) continue;

            EventHandler<?> handler = new MethodEventHandler<>(obj, method, (Class<Event>) eventClass);
            register(handler);
            handlers.add(handler);
        }

        objects.put(obj, handlers);
    }

    @Override
    public void unregister(Object obj) {
        List<EventHandler<?>> handlers = objects.get(obj);
        if (handlers == null) return;
        for (EventHandler<?> handler : handlers) {
            unregister(handler);
        }
    }

    @RequiredArgsConstructor
    private static class MethodEventHandler<T extends Event> implements EventHandler<T> {

        private final Object object;
        private final Method method;

        @Getter
        private final Class<T> eventClass;

        @Override
        public void handle(T event) throws Exception {
            method.setAccessible(true);
            method.invoke(object, event);
        }
    }
}
