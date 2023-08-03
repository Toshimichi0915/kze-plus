package net.toshimichi.kzeplus.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WeaponReward {

    // assault rifles
    M4A1("M4A1", 30),
    AN_94("AN-94", 38),
    FAMAS("FAMAS", 39),
    TYPE_95("Type-95", 35),
    HK417("HK417", 64),
    SCAR_L("SCAR-L", 41),

    // shotguns
    FuryII("FuryII", 25),

    // submachine guns
    MP5("MP5", 28),
    MP7("MP7", 29),
    OA93("OA-93", 30),
    MAC_10("MAC-10", 32),
    UZI("UZI", 24),
    TMP("TMP", 31),
    FMG9("FMG9", 34),
    Vz_61("Vz-61", 31),
    UMP45("UMP", 33),
    BIZON_PP19("Bizon-PP19", 35),
    P90("P90", 30),
    K1A1("K1A1", 37),
    HoneyBadger("HoneyBadger", 36),

    // sniper rifles
    SCOUT_ELITE("ScoutElite", 113),
    M1855("M1855", 58),
    M110("M110", 102),
    PSG_1("PSG-1", 95),
    M1_GARAND("M1garand", 52),
    WA2000("WA2000", 146),
    L96A1("L96A1", 146),
    BARRETT_M82("BarrettM82", 154),
    M14("M14", 121),

    // handguns
    GLOCK_17("Glock17", 13),
    TEC_9("Tec-9", 10),
    M92FS("M92FS", 12),
    RUGAR_MKI("Rugar-MkI", 22),
    S_WM37("S&WM37", 27),
    BARETTA_M93R("Baretta M93R", 12),
    TAURUS_JUDGE("TaurusJudge", 16),
    USP_45("USP45", 23),
    SAA("SAA", 31),
    FIVE_SEVEN("Five seveN", 14),
    G2_CONTENDER("G2 contender", 56);

    private final String name;
    private final int reward;

    public static WeaponReward fromName(String name) {
        for (WeaponReward weaponReward : values()) {
            if (weaponReward.getName().equals(name)) {
                return weaponReward;
            }
        }
        return null;
    }
}
