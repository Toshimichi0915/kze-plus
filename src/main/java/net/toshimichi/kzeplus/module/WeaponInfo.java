package net.toshimichi.kzeplus.module;

import lombok.Data;

import java.util.Objects;

@Data
class WeaponInfo {

    private final String name;
    private final int magazineSize;
    private final int reloadTicks;
    private final int bulletPerReload;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponInfo that = (WeaponInfo) o;
        return magazineSize == that.magazineSize && reloadTicks == that.reloadTicks && bulletPerReload == that.bulletPerReload && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, magazineSize, reloadTicks, bulletPerReload);
    }
}
