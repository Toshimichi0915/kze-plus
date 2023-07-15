package net.toshimichi.kzeplus.events;

public interface EventRegistry {

    void register(EventHandler<?> handler);

    void unregister(EventHandler<?> handler);

    void register(Object object);

    void unregister(Object object);

    void call(Event event);
}
