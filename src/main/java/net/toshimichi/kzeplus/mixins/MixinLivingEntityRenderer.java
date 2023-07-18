package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.toshimichi.kzeplus.options.VisibilityMode;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "getRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getItemEntityTranslucentCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void fixRenderLayer(LivingEntity livingEntity, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<@Nullable RenderLayer> cir, Identifier identifier) {
        if (!KzeUtils.isInKze()) return;
        if (!livingEntity.isInvisible()) return;

        cir.setReturnValue(RenderLayer.getEntityTranslucentEmissive(identifier));
        cir.cancel();
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void hidePlayer(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!KzeUtils.shouldHide(livingEntity)) return;

        VisibilityMode mode = KzeUtils.getVisibilityMode();
        if (mode != VisibilityMode.NONE) return;
        ci.cancel();
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    public void cancelFeatureRender(FeatureRenderer<Entity, ?> instance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, Entity t, float f1, float f2, float f3, float f4, float f5, float f6) {
        VisibilityMode mode = KzeUtils.getVisibilityMode();
        if (KzeUtils.shouldHide(t) && mode != VisibilityMode.FULL) return;

        instance.render(matrixStack, vertexConsumerProvider, i, t, f1, f2, f3, f4, f5, f6);
    }
}
