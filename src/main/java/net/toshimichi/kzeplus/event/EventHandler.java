package net.toshimichi.kzeplus.event;

public interface EventHandler<T extends Event> {

    void handle(T event) throws Exception;

    Class<T> getEventClass();
}
