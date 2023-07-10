package net.toshimichi.kzeplus.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.options.KzeOptions;
import net.toshimichi.kzeplus.options.VisibilityMode;

import java.util.List;

public class KzeUtils {

    public static boolean isInKze() {
        ServerInfo server = MinecraftClient.getInstance().getCurrentServerEntry();
        if (server == null) return false;
        return server.address.contains("kze.network");
    }

    private static List<AbstractClientPlayerEntity> getTeamPlayers(String name) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return List.of();

        Team team = world.getScoreboard().getTeam(name);
        if (team == null) return List.of();

        return world.getPlayers()
                .stream()
                .filter(player -> team.getPlayerList().contains(player.getEntityName()))
                .toList();
    }

    public static List<AbstractClientPlayerEntity> getSurvivors() {
        return getTeamPlayers("e");
    }

    public static List<AbstractClientPlayerEntity> getZombies() {
        return getTeamPlayers("z");
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
        KzeOptions options = KzePlusMod.getInstance().getOptions();
        VisibilityMode mode = KzePlusMod.getInstance().getDefaultVisibility();

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
