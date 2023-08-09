package net.toshimichi.kzeplus.context.weapon;


import lombok.Data;
import net.toshimichi.kzeplus.KzePlus;

@Data
public class WeaponContext {

    private final int slot;

    // retrieved from item stack
    private boolean valid;
    private String name;
    private int currentAmmo;
    private int remainingAmmo;
    private boolean reloading;

    // context dependant
    private int reloadTicks;

    public WeaponInfo getWeaponInfo() {
        WeaponInfo info = KzePlus.getInstance().getWeaponRegistry().load(name);
        if (info == null) {
            info = new WeaponInfo(name, currentAmmo, 0, 0, 0);
            KzePlus.getInstance().getWeaponRegistry().save(info);
        }
        return info;
    }

    public double getRemainingReloadTicks() {
        if (!reloading) return 0;

        WeaponInfo weaponInfo = getWeaponInfo();
        int maxReloadTicks = weaponInfo.getReloadTicks();
        if (maxReloadTicks == 0) return 0;

        int reloadAmount = Math.min(weaponInfo.getMagazineSize() - currentAmmo, remainingAmmo);
        double timesToReload = Math.max((double) reloadAmount / weaponInfo.getBulletPerReload(), 1);
        return Math.max(timesToReload * weaponInfo.getReloadTicks() - reloadTicks, 0);
    }

    public double getTotalReloadTicks() {
        if (!reloading) return 0;

        WeaponInfo weaponInfo = getWeaponInfo();
        int maxReloadTicks = weaponInfo.getReloadTicks();
        if (maxReloadTicks == 0) return 0;

        double timesToReload = Math.max((double) weaponInfo.getMagazineSize() / weaponInfo.getBulletPerReload(), 1);
        return Math.max(timesToReload * weaponInfo.getReloadTicks(), 0);
    }
}
