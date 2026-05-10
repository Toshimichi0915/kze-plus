package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
    private void notifyRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        KzePlus.getInstance().getEventRegistry().call(new InGameHudRenderEvent(context, tickCounter.getTickProgress(false)));
    }
}
