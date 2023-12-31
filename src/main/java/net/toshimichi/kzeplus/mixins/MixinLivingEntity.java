package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", cancellable = true)
    private void addNightVision(StatusEffect effect, CallbackInfoReturnable<Boolean> info) {
        if (!KzeUtils.isInKze()) return;
        if (!KzePlus.getInstance().getOptions().isFullBright()) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != (Object) this || effect != StatusEffects.NIGHT_VISION) return;

        info.setReturnValue(true);
        info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "getStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/entity/effect/StatusEffectInstance;", cancellable = true)
    private void returnNightVision(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> info) {
        if (!KzeUtils.isInKze()) return;
        if (!KzePlus.getInstance().getOptions().isFullBright()) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != (Object) this || effect != StatusEffects.NIGHT_VISION) return;

        info.setReturnValue(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE));
        info.cancel();
    }
}
