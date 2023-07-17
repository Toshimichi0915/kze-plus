package net.toshimichi.kzeplus.modules;

import lombok.Data;

@Data
class WeaponInfo {

    private final String name;
    private final int magazineSize;
    private final int reloadTicks;
    private final int bulletPerReload;
}