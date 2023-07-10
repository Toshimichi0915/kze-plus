package net.toshimichi.kzeplus.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class MixinChatHud {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), cancellable = true)
    public void notifyChat(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        KzePlusMod.getInstance().getEventRegistry().call(event);

        if (event.isCancelled()) ci.cancel();
    }
}
