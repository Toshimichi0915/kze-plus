package net.toshimichi.kzeplus.modules;

import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
class WeaponStatus {

    private static final Pattern WEAPON_NAME_PATTERN = Pattern.compile("([^ ]+?) +(\\d+?) +(\\d+)");
    private static final int RELOAD_COLOR = 0xfc5454;

    private final String name;
    private final int currentAmmo;
    private final int remainingAmmo;
    private final boolean reloading;

    private static boolean hasRedName(Text text) {
        if (text.getSiblings().stream().anyMatch(WeaponStatus::hasRedName)) return true;

        Style style = text.getStyle();
        if (style == null) return false;

        TextColor color = style.getColor();
        if (color == null) return false;

        return "red".equals(color.getName());
    }

    public static WeaponStatus fromItemStack(ItemStack itemStack) {
        Matcher matcher = WEAPON_NAME_PATTERN.matcher(itemStack.getName().getString());
        if (!matcher.find()) return null;

        String name = matcher.group(1);
        int currentAmmo = Integer.parseInt(matcher.group(2));
        int remainingAmmo = Integer.parseInt(matcher.group(3));

        return new WeaponStatus(name, currentAmmo, remainingAmmo, hasRedName(itemStack.getName()));
    }
}
