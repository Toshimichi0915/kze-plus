package net.toshimichi.kzeplus.module;

import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ClientTickEvent;
import net.toshimichi.kzeplus.event.EventTarget;
import net.toshimichi.kzeplus.options.VisibilityMode;

public class VisibiiltyToggleModule implements Module {

    private boolean prevPressed;

    @Override
    public void onEnable() {
        KzePlusMod.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlusMod.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    public void toggleVisibility(ClientTickEvent e) {
        boolean pressed = KzePlusMod.VISIBILITY_TOGGLE_KEY.isPressed();

        if (prevPressed == pressed) return;
        prevPressed = pressed;

        if (!pressed) return;

        VisibilityMode current = KzePlusMod.getInstance().getDefaultVisibility();
        VisibilityMode next = VisibilityMode.values()[(current.ordinal() + 1) % VisibilityMode.values().length];
        KzePlusMod.getInstance().setDefaultVisibility(next);
    }
}
