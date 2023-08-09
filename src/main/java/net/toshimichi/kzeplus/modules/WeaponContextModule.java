package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.weapon.WeaponInfo;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeaponContextModule implements Module {

    private static final Pattern WEAPON_NAME_PATTERN = Pattern.compile("^(.+?) +(\\d+?) +(\\d+)$");

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    private void updateWeaponContexts(ClientTickEvent e) {
        updateWeaponContext(KzePlus.getInstance().getMainWeaponContext());
        updateWeaponContext(KzePlus.getInstance().getSubWeaponContext());
    }

    private static boolean hasRedName(Text text) {
        if (text.getSiblings().stream().anyMatch(WeaponContextModule::hasRedName)) return true;

        Style style = text.getStyle();
        if (style == null) return false;

        TextColor color = style.getColor();
        if (color == null) return false;

        return "red".equals(color.getName());
    }

    private void updateWeaponContext(WeaponContext context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        ItemStack itemStack = player.getInventory().getStack(context.getSlot());

        Matcher matcher = WEAPON_NAME_PATTERN.matcher(itemStack.getName().getString());
        if (!matcher.find()) {
            context.setValid(false);
            context.setReloadTicks(0);
            return;
        }

        String name = matcher.group(1);
        int currentAmmo = Integer.parseInt(matcher.group(2));
        int remainingAmmo = Integer.parseInt(matcher.group(3));
        boolean reloading = hasRedName(itemStack.getName());

        int ammoDiff = currentAmmo - context.getCurrentAmmo();
        boolean prevAmmoEmpty = context.getCurrentAmmo() == 0;
        boolean prevReloading = context.isReloading();

        context.setValid(true);
        context.setName(name);
        context.setCurrentAmmo(currentAmmo);
        context.setRemainingAmmo(remainingAmmo);
        context.setReloading(reloading);

        WeaponInfo weaponInfo = context.getWeaponInfo();

        // update reload related info
        if (context.getReloadTicks() > 0 && ammoDiff > 0 && (prevAmmoEmpty || currentAmmo != weaponInfo.getMagazineSize())) {
            weaponInfo = new WeaponInfo(weaponInfo.getName(), weaponInfo.getMagazineSize(), context.getReloadTicks(), ammoDiff, weaponInfo.getRewardPerHit());
            context.setReloadTicks(0);
            KzePlus.getInstance().getWeaponRegistry().save(weaponInfo);
        }

        if (reloading && player.getInventory().selectedSlot == context.getSlot()) {
            context.setReloadTicks(context.getReloadTicks() + 1);
        }

        if (prevReloading && !reloading) {
            context.setReloadTicks(0);
        }
    }
}
