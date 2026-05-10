package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "getRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;itemEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void fixRenderLayer(LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<@Nullable RenderLayer> cir, Identifier identifier) {
        if (!KzeUtils.isInKze()) return;
        if (!state.invisible) return;

        cir.setReturnValue(RenderLayers.entityTranslucentEmissive(identifier));
        cir.cancel();
    }
}
