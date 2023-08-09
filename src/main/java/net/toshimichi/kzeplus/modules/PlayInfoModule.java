package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.game.GameContext;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.time.Duration;

public class PlayInfoModule implements Module {

    private int playTime;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        playTime = 0;
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

        int reward = KzePlus.getInstance().getGameContextRegistry().getGameContextHistories()
                .stream()
                .mapToInt(GameContext::getReward)
                .sum();

        int exp = KzePlus.getInstance().getGameContextRegistry().getGameContextHistories()
                .stream()
                .mapToInt(GameContext::getExp)
                .sum();

        int defenseBonus = 0;
        int mainDefenseCount = 0;
        int subDefenseCount = 0;
        int hitBonus = 0;
        int hitCount = 0;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context != null) {
            defenseBonus = context.getEstimatedDefenseBonus();
            mainDefenseCount = context.getMainHitCount();
            subDefenseCount = context.getSubHitCount();
            hitBonus = context.getEstimatedHitBonus();
            hitCount = context.getShotCount();
        }

        InGameHud.fill(e.getMatrices(), 20, 20, 145, 120, 0x80000000);

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

        // bonus
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "防衛: " + Math.min(defenseBonus, 10000) + "円 (" + mainDefenseCount + ", " + subDefenseCount + ")", 25, 95, 0xffffff);
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "被弾: " + (int) Math.min(hitBonus, 4000) + "円 (" + hitCount + ")", 25, 105, 0xffffff);
    }
}
