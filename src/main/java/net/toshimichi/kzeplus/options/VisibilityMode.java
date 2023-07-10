package net.toshimichi.kzeplus.options;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public enum VisibilityMode {
    FULL("kze_plus.visibility.full"),
    OUTLINE("kze_plus.visibility.outline"),
    NONE("kze_plus.visibility.none");

    public static final Codec<VisibilityMode> CODEC = Codec.STRING.xmap(VisibilityMode::valueOf, VisibilityMode::name);
    private final String key;

    public static SimpleOption<VisibilityMode> createSimpleOption(String key, VisibilityMode defaultValue, Consumer<VisibilityMode> callback) {
        return new SimpleOption<>(
                key,
                SimpleOption.emptyTooltip(),
                (t, v) -> Text.translatable(v.getKey()),
                new SimpleOption.LazyCyclingCallbacks<>(() -> List.of(VisibilityMode.values()), Optional::of, CODEC),
                defaultValue,
                callback);
    }
}
