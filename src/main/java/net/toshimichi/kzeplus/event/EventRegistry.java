package net.toshimichi.kzeplus.event;

public interface EventRegistry {

    void register(EventHandler<?> handler);

    void unregister(EventHandler<?> handler);

    void register(Object object);

    void unregister(Object object);

    void call(Event event);
}
