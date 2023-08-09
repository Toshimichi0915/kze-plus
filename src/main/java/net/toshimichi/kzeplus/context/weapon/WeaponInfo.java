package net.toshimichi.kzeplus.context.weapon;

import lombok.Data;

@Data
public class WeaponInfo {

    private final String name;
    private final int magazineSize;
    private final int reloadTicks;
    private final int bulletPerReload;
    private final int rewardPerHit;
}
