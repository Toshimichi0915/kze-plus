package net.toshimichi.kzeplus.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.options.KzeOptions;

import java.util.List;

public class KzeUtils {

    public static boolean isInKze() {
//        ServerInfo server = MinecraftClient.getInstance().getCurrentServerEntry();
//        if (server == null) return false;
//        return server.address.contains("kze.network");
        return true;
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

    public static KzeOptions.VisibilityMode getVisibilityMode() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return KzeOptions.VisibilityMode.FULL;

        KzeOptions options = KzePlusMod.getInstance().getOptions();

        if (player.isSneaking()) return options.getHideOnSneak();

        GameOptions gameOptions = MinecraftClient.getInstance().options;
        if (player.isSprinting() || gameOptions.forwardKey.isPressed() && gameOptions.sprintKey.isPressed()) return options.getHideOnSprint();

        return KzeOptions.VisibilityMode.FULL;
    }
}
