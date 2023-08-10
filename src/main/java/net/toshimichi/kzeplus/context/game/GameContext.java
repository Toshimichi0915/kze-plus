package net.toshimichi.kzeplus.context.game;

import lombok.Data;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.weapon.WeaponInfo;

@Data
public class GameContext {

    private final long startedAt;
    private long endedAt;
    private boolean ended;
    private boolean humanWin;

    private int reward;
    private int exp;

    private int defenseBonus;
    private int hitBonus;

    private String mainWeaponName;
    private int mainHitCount;

    private String subWeaponName;
    private int subHitCount;

    private int shotCount;
    private double damage;

    public WeaponInfo getMainWeapon() {
        WeaponInfo info = KzePlus.getInstance().getWeaponRegistry().load(mainWeaponName);
        if (info == null) return new WeaponInfo(mainWeaponName, 0, 0, 0, 0);

        return info;
    }

    public WeaponInfo getSubWeapon() {
        WeaponInfo info = KzePlus.getInstance().getWeaponRegistry().load(subWeaponName);
        if (info == null) info = new WeaponInfo(mainWeaponName, 0, 0, 0, 0);

        return info;
    }

    public int getEstimatedDefenseBonus() {
        return getMainWeapon().getRewardPerHit() * mainHitCount + getSubWeapon().getRewardPerHit() * subHitCount;
    }

    public int getEstimatedHitBonus() {
        return (int) (damage + shotCount * 5);
    }
}
