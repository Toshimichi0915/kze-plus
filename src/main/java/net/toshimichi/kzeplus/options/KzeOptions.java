package net.toshimichi.kzeplus.options;

import lombok.Data;

@Data
public class KzeOptions {

    private VisibilityMode hideOnSneak = VisibilityMode.OUTLINE;
    private VisibilityMode hideOnSprint = VisibilityMode.OUTLINE;
    private boolean fullBright = true;
    private boolean noFog = true;
    private boolean hideMagazineMessage = true;
    private boolean hideKillMessage = true;
    private boolean showKillLog = true;
    private boolean showTimer = true;
}
