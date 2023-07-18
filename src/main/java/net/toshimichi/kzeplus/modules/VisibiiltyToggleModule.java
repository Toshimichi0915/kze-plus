package net.toshimichi.kzeplus.modules;

import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.options.VisibilityMode;

public class VisibiiltyToggleModule implements Module {

    private boolean prevPressed;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    private void toggleVisibility(ClientTickEvent e) {
        boolean pressed = KzePlus.VISIBILITY_TOGGLE_KEY.isPressed();

        if (prevPressed == pressed) return;
        prevPressed = pressed;

        if (!pressed) return;

        VisibilityMode current = KzePlus.getInstance().getDefaultVisibility();
        VisibilityMode next = VisibilityMode.values()[(current.ordinal() + 1) % VisibilityMode.values().length];
        KzePlus.getInstance().setDefaultVisibility(next);
    }
}
