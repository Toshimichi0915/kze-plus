package net.toshimichi.kzeplus.options;

import lombok.Data;

@Data
public class KzeOptions {

    private VisibilityMode hideOnSneak = VisibilityMode.OUTLINE;
    private VisibilityMode hideOnSprint = VisibilityMode.OUTLINE;
    private double gunSoundVolume = 1;
    private double damageSoundVolume = 1;
    private double hitSoundVolume = 1;
    private boolean fullBright = true;
    private boolean noFog = true;
    private boolean hideMagazineMessage = true;
    private boolean hideKillMessage = true;
    private boolean showKillLog = true;
    private boolean showTimer = true;
    private boolean showReloadProgress = true;
    private boolean showWeaponInfo = true;
    private boolean showReward = true;
    private boolean showExp = true;
    private boolean showPlayTime = true;
    private boolean showTeam = true;
    private boolean showBonus = true;
    private boolean showNextVote = true;
    private boolean showNextVoteOnlyWhenAvailable = false;
    private boolean hideVoteMessage = true;
}
