package net.toshimichi.kzeplus.modules;

import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.widget.WidgetLayout;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;

public class WidgetContextModule implements Module {

    private boolean flushed;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    private void renderWidgets(InGameHudRenderEvent e) {
        if (!flushed) {
            KzePlus.getInstance().getWidgetContext().flush();
            flushed = true;
        }

        WidgetLayout root = KzePlus.getInstance().getWidgetContext().getRoot();
        root.relocate(null, false);
        root.render(e.getMatrices(), e.getTickDelta());
    }
}
