package net.toshimichi.kzeplus.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;

@RequiredArgsConstructor
@Getter
public enum GameRole {

    SURVIVOR("e", 0x00a8a8),
    ZOMBIE("z", 0x54fb54);

    private final String teamName;
    private final int color;

    public static GameRole fromEntity(Entity entity) {
        if (entity == null) return null;

        AbstractTeam team = entity.getScoreboardTeam();
        if (team == null) return null;

        for (GameRole role : values()) {
            if (role.getTeamName().equals(team.getName())) return role;
        }

        return null;
    }
}
