package net.toshimichi.kzeplus.mixins;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.toshimichi.kzeplus.KzePlus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Mixin(Language.class)
public class MixinLanguage {

    @Final @Shadow
    private static Pattern TOKEN_PATTERN;
    @Final @Shadow
    private static Gson GSON;

    @Inject(method = "load", at = @At("HEAD"))
    private static void addCustomKeys(InputStream inputStream, BiConsumer<String, String> entryConsumer, CallbackInfo ci) {
        Path path = KzePlus.getInstance()
                .getModContainer()
                .findPath("assets/kze_plus/lang/en_us.json")
                .orElseThrow();

        try (InputStream in = path.toUri().toURL().openStream()) {
            JsonObject jsonObject = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String string = TOKEN_PATTERN.matcher(JsonHelper.asString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
                entryConsumer.accept(entry.getKey(), string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
