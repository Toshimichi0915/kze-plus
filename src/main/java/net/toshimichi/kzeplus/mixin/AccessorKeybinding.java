package net.toshimichi.kzeplus.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface AccessorKeybinding {

    @Accessor("CATEGORY_ORDER_MAP")
    static Map<String, Integer> getCategoryOrderMap() {
        throw new RuntimeException();
    }

    @Accessor("CATEGORY_ORDER_MAP")
    static void setCategoryOrderMap(Map<String, Integer> categoryOrderMap) {
        throw new RuntimeException();
    }
}
