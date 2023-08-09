package net.toshimichi.kzeplus.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;

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

    public static GameRole fromName(String name) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return null;

        PlayerListEntry entry = networkHandler.getPlayerListEntry(name);
        if (entry == null) return null;

        Team team = entry.getScoreboardTeam();
        if (team == null) return null;

        for (GameRole role : values()) {
            if (role.getTeamName().equals(team.getName())) return role;
        }

        return null;
    }
}
