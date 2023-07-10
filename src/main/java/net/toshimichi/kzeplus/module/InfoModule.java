package net.toshimichi.kzeplus.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ChatEvent;
import net.toshimichi.kzeplus.event.ClientTickEvent;
import net.toshimichi.kzeplus.event.EventTarget;
import net.toshimichi.kzeplus.event.InGameHudRenderEvent;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoModule implements Module {

    private static final Pattern REWARD_PATTERN = Pattern.compile("合計 (\\d+)円");
    private static final Pattern EXP_PATTERN = Pattern.compile("取得パーク経験値 \\+(\\d+) \\(ナイフ武器\\)");

    private int reward;
    private int exp;
    private int playTime;

    @Override
    public void onEnable() {
        KzePlusMod.getInstance().getEventRegistry().register(this);
        reward = 0;
    }

    @Override
    public void onDisable() {
        KzePlusMod.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    private void updateReward(ChatEvent e) {
        String text = e.getText().getString();
        if (!text.startsWith("》")) return;
        Matcher matcher = REWARD_PATTERN.matcher(text);
        if (matcher.find()) {
            reward += Integer.parseInt(matcher.group(1));
        }

        matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            exp += Integer.parseInt(matcher.group(1));
        }
    }

    @EventTarget
    private void updatePlayTime(ClientTickEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (!KzeUtils.getSurvivors().contains(player) && !KzeUtils.getZombies().contains(player)) return;

        playTime++;
    }

    @EventTarget
    private void renderInfo(InGameHudRenderEvent e) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        InGameHud.fill(e.getMatrices(), 20, 20, 125, 60, 0x80000000);

        // reward
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得金額: " + reward + "円", 25, 25, 0xFFFFFF);

        // exp
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得経験値: " + exp, 25, 35, 0xFFFFFF);

        // play time
        Duration duration = Duration.ofSeconds(playTime / 20);
        String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "プレイ時間: " + time, 25, 45, 0xFFFFFF);
    }
}
