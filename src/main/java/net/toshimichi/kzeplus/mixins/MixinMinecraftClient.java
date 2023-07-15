package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.MinecraftClient;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick", at = @At("HEAD"))
    public void notifyTick(CallbackInfo ci) {
        KzePlus.getInstance().getEventRegistry().call(new ClientTickEvent());
    }
}
