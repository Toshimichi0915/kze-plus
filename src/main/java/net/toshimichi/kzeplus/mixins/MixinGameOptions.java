package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.toshimichi.kzeplus.KzePlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {

    @Shadow public KeyBinding[] allKeys;

    @Inject(method = "load", at = @At("HEAD"))
    private void registerKeys(CallbackInfo ci) {

        // add new keys
        KeyBinding[] toAdd = new KeyBinding[]{KzePlus.VISIBILITY_TOGGLE_KEY};
        KeyBinding[] newAllKeys = new KeyBinding[allKeys.length + toAdd.length];
        System.arraycopy(allKeys, 0, newAllKeys, 0, allKeys.length);
        System.arraycopy(toAdd, 0, newAllKeys, allKeys.length, toAdd.length);
        allKeys = newAllKeys;

        // add new categories
        KeyBinding.CATEGORY_ORDER_MAP.put(KzePlus.KZE_CATEGORY, 10000);
    }
}
