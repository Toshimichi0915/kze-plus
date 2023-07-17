package net.toshimichi.kzeplus.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.options.KzeOptions;
import net.toshimichi.kzeplus.options.VisibilityMode;

public class KzeUtils {

    public static boolean isInKze() {
        ServerInfo server = MinecraftClient.getInstance().getCurrentServerEntry();
        if (server == null) return false;
        return server.address.contains("kze.network");
    }

    public static boolean isInGame() {
        if (!isInKze()) return false;

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return false;

        return world.getScoreboard().getTeam("sb") != null;
    }

    private static int countTeamPlayers(String name) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return 0;

        Team team = world.getScoreboard().getTeam(name);
        if (team == null) return 0;

        return (int) player.networkHandler.getListedPlayerListEntries()
                .stream()
                .filter(entry -> team.getPlayerList().contains(entry.getProfile().getName()))
                .count();
    }

    public static int getSurvivorCount() {
        return countTeamPlayers(GameRole.SURVIVOR.getTeamName());
    }

    public static int getZombieCount() {
        return countTeamPlayers(GameRole.ZOMBIE.getTeamName());
    }

    public static boolean shouldHide(Entity entity) {
        if (!isInKze()) return false;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        if (player == entity) return false;
        if (player.getScoreboardTeam() != entity.getScoreboardTeam()) return false;

        return true;
    }

    public static VisibilityMode getVisibilityMode() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return VisibilityMode.FULL;

        GameOptions gameOptions = MinecraftClient.getInstance().options;
        KzeOptions options = KzePlus.getInstance().getOptions();
        VisibilityMode mode = KzePlus.getInstance().getDefaultVisibility();

        if (player.isSneaking()) {
            VisibilityMode candidate = options.getHideOnSneak();
            if (candidate.ordinal() > mode.ordinal()) mode = candidate;
        }

        if (player.isSprinting() || gameOptions.forwardKey.isPressed() && gameOptions.sprintKey.isPressed()) {
            VisibilityMode candidate = options.getHideOnSprint();
            if (candidate.ordinal() > mode.ordinal()) mode = candidate;
        }

        return mode;
    }
}
