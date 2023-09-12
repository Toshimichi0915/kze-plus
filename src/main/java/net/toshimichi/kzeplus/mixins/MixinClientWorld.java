package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.SoundPlayEvent;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @ModifyArgs(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/PositionedSoundInstance;<init>(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFLnet/minecraft/util/math/random/Random;DDD)V"))
    private void applyGunSoundVolume(Args args) {
        SoundEvent event = args.get(0);
        float volume = args.get(2);
        if (event.getId().getNamespace().equals("minecraft") && KzePlus.getInstance().getGunShotSounds().contains(event.getId().getPath())) {
            args.set(2, (float) (volume * KzePlus.getInstance().getOptions().getGunSoundVolume()));
        } else if (event == SoundEvents.ENTITY_PLAYER_HURT && KzeUtils.isInGame()) {
            args.set(2, (float) (volume * KzePlus.getInstance().getOptions().getDamageSoundVolume()));
        } else if (event == SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP && KzeUtils.isInGame()) {
            args.set(2, (float) (volume * KzePlus.getInstance().getOptions().getHitSoundVolume()));
        }
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"))
    private void notifyPlaySound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        KzePlus.getInstance().getEventRegistry().call(new SoundPlayEvent(x, y, z, event, category, volume, pitch, useDistance, seed));
    }
}
