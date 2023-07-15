package net.toshimichi.kzeplus.mixins;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptions.class)
public interface AccessorGameOptions {

    @Accessor("allKeys")
    KeyBinding[] getAllKeys();

    @Accessor("allKeys")
    @Mutable
    void setAllKeys(KeyBinding[] allKeys);
}
