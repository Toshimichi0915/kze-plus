package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.SoundPlayEvent;
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
        if (!event.getId().getNamespace().equals("minecraft")) return;
        if (!KzePlus.getInstance().getGunShotSounds().contains(event.getId().getPath())) return;

        args.set(2, (float) KzePlus.getInstance().getOptions().getGunSoundVolume());
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"))
    private void notifyPlaySound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        KzePlus.getInstance().getEventRegistry().call(new SoundPlayEvent(x, y, z, event, category, volume, pitch, useDistance, seed));
    }
}
