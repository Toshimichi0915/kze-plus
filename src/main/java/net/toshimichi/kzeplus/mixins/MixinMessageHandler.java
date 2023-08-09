package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MixinMessageHandler {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void notifyChat(Text message, boolean overlay, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        KzePlus.getInstance().getEventRegistry().call(event);

        if (event.isCancelled()) ci.cancel();
    }
}
