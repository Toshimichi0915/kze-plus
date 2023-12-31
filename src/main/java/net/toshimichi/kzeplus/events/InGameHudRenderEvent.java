package net.toshimichi.kzeplus.events;

import lombok.Data;
import net.minecraft.client.util.math.MatrixStack;

@Data
public class InGameHudRenderEvent implements Event {

    private final MatrixStack matrices;
    private final float tickDelta;
}
