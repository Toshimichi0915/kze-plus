package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayInfoModule implements Module {

    private static final Pattern REWARD_PATTERN = Pattern.compile("》 +合計 \\+?(\\d+?)円");
    private static final Pattern EXP_PATTERN = Pattern.compile("》 +取得パーク経験値 \\+?(\\d+?) \\(ナイフ武器\\)");

    private int reward;
    private int exp;
    private int playTime;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        reward = 0;
        exp = 0;
        playTime = 0;
    }

    @EventTarget
    private void updateReward(ChatEvent e) {
        String text = e.getText().getString();
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
        if (!KzeUtils.isInGame()) return;

        playTime++;
    }

    @EventTarget
    private void renderInfo(InGameHudRenderEvent e) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        InGameHud.fill(e.getMatrices(), 20, 20, 125, 90, 0x80000000);

        // reward
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得金額: " + reward + "円", 25, 25, 0xffffff);

        // exp
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得経験値: " + exp, 25, 35, 0xffffff);

        // play time
        Duration duration = Duration.ofSeconds(playTime / 20);
        String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "プレイ時間: " + time, 25, 45, 0xffffff);

        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "生存者: " + KzeUtils.getSurvivorCount() + "人", 25, 65, 0xffffff);
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "ゾンビ: " + KzeUtils.getZombieCount() + "人", 25, 75, 0xffffff);
    }
}