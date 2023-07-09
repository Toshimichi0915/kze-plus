package net.toshimichi.kzeplus.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(MessageHandler.class)
public class MixinMessageHandler {

    @Inject(method = "addToChatLog(Lnet/minecraft/text/Text;Ljava/time/Instant;)V", at = @At("HEAD"))
    public void notifyChat(Text message, Instant timestamp, CallbackInfo ci) {
        KzePlusMod.getInstance().getEventRegistry().call(new ChatEvent(message));

    }

    @Inject(method = "addToChatLog(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/network/message/MessageType$Parameters;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/network/message/MessageTrustStatus;)V", at = @At("HEAD"))
    public void notifyChat0(SignedMessage message, MessageType.Parameters params, GameProfile sender, MessageTrustStatus trustStatus, CallbackInfo ci) {
        KzePlusMod.getInstance().getEventRegistry().call(new ChatEvent(message.getContent()));
    }
}
