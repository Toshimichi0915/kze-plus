package net.toshimichi.kzeplus.events;

public interface EventHandler<T extends Event> {

    void handle(T event) throws Exception;

    Class<T> getEventClass();
}
