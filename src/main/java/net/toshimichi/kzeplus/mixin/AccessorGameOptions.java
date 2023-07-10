package net.toshimichi.kzeplus.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptions.class)
public interface AccessorGameOptions {

    @Accessor("allKeys")
    KeyBinding[] getAllKeys();

    @Accessor("allKeys")
    void setAllKeys(KeyBinding[] allKeys);
}
