package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("TAIL"))
    private void notifyRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        KzePlus.getInstance().getEventRegistry().call(new InGameHudRenderEvent(matrices, tickDelta));
    }
}
