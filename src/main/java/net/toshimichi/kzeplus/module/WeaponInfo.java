package net.toshimichi.kzeplus.module;

import lombok.Data;

@Data
class WeaponInfo {

    private final String name;
    private final int magazineSize;
    private final int reloadTicks;
    private final int bulletPerReload;
}
