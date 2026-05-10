package net.toshimichi.kzeplus.events;

import lombok.Data;
import net.minecraft.client.gui.DrawContext;

@Data
public class InGameHudRenderEvent implements Event {

    private final DrawContext context;
    private final float tickDelta;
}
