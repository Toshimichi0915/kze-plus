package net.toshimichi.kzeplus.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    private static final float FOG_START = -8;
    private static final float FOG_END = 1000000;

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void disableSkyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (!KzeUtils.isInKze()) return;
        if (!KzePlusMod.getInstance().getOptions().isNoFog()) return;

        CameraSubmersionType type = camera.getSubmersionType();
        if (type != CameraSubmersionType.WATER &&
                fogType != BackgroundRenderer.FogType.FOG_TERRAIN &&
                fogType != BackgroundRenderer.FogType.FOG_SKY) return;

        RenderSystem.setShaderFogStart(FOG_START);
        RenderSystem.setShaderFogEnd(FOG_END);
        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
        ci.cancel();
    }
}
