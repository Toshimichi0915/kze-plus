package net.toshimichi.kzeplus.modules;

import net.toshimichi.kzeplus.context.widget.Widget;

import java.util.Map;

public interface Module {

    default void onEnable() {

    }

    default void onDisable() {

    }

    default Map<String, Widget> getWidgets() {
        return Map.of();
    }
}
