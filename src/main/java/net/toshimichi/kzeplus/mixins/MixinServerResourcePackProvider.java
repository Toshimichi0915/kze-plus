package net.toshimichi.kzeplus.mixins;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resource.ServerResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.toshimichi.kzeplus.KzePlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(ServerResourcePackProvider.class)
public class MixinServerResourcePackProvider {

    @Unique private static final Gson GSON = new Gson();

    @Inject(method = "loadServerPack(Ljava/io/File;Lnet/minecraft/resource/ResourcePackSource;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    public void updateKzeSounds(File packZip, ResourcePackSource packSource, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        try (ZipFile zipFile = new ZipFile(packZip)) {
            ZipEntry entry = zipFile.getEntry("assets/minecraft/sounds.json");
            if (entry == null) return;

            JsonObject soundsObj;
            try (InputStream in = zipFile.getInputStream(entry)) {
                soundsObj = GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
            }

            KzePlus.getInstance().getGunShotSounds().clear();
            for (String key : soundsObj.keySet()) {
                JsonArray soundSet = soundsObj.get(key)
                        .getAsJsonObject()
                        .get("sounds")
                        .getAsJsonArray();

                boolean gunSound = false;
                for (JsonElement element : soundSet) {
                    String path = element.getAsJsonObject()
                            .get("name")
                            .getAsString();

                    if (path.startsWith("gunshot/")) {
                        gunSound = true;
                        break;
                    }
                }

                if (gunSound) {
                    KzePlus.getInstance().getGunShotSounds().add(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
