package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.game.GameContext;
import net.toshimichi.kzeplus.context.widget.Widget;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class PlayInfoModule implements Module {

    private boolean enabled;
    private int playTime;

    @Override
    public void onEnable() {
        enabled = true;
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        enabled = false;
        KzePlus.getInstance().getEventRegistry().unregister(this);
        playTime = 0;
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return Map.of("play_info", new PlayInfoWidget());
    }

    @EventTarget
    private void updatePlayTime(ClientTickEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (!KzeUtils.isInGame()) return;

        playTime++;
    }

    private class PlayInfoWidget implements Widget {

        private int height;
        private boolean showReward;
        private boolean showPlayTime;
        private boolean showTeam;
        private boolean showBonus;

        @Override
        public void update(boolean placeholder) {
            showReward = KzePlus.getInstance().getOptions().isShowReward();
            showPlayTime = KzePlus.getInstance().getOptions().isShowPlayTime();
            showTeam = KzePlus.getInstance().getOptions().isShowTeam();
            showBonus = KzePlus.getInstance().getOptions().isShowBonus();
        }

        @Override
        public void render(int x, int y, DrawContext context, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            int reward = KzePlus.getInstance().getGameContextRegistry().getGameContextHistories()
                    .stream()
                    .mapToInt(GameContext::getReward)
                    .sum();

            int defenseBonus = 0;
            int mainDefenseCount = 0;
            int subDefenseCount = 0;
            int hitBonus = 0;
            int hitCount = 0;

            GameContext gameContext = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
            if (gameContext != null) {
                defenseBonus = gameContext.getEstimatedDefenseBonus();
                mainDefenseCount = gameContext.getMainHitCount();
                subDefenseCount = gameContext.getSubHitCount();
                hitBonus = gameContext.getEstimatedHitBonus();
                hitCount = gameContext.getShotCount();
            }

            if (!showReward && !showPlayTime && !showTeam && !showBonus) {
                height = 0;
                return;
            }
            context.fill(x, y, x + getWidth(), y + getHeight(), 0x80000000);

            int delta = 5;


            // reward
            if (showReward) {
                context.drawTextWithShadow(textRenderer, "取得金額: " + reward + "円", x + 5, y + delta, 0xffffffff);
                delta += 10;
            }

            // play time
            if (showPlayTime) {
                Duration duration = Duration.ofSeconds(playTime / 20);
                String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
                context.drawTextWithShadow(textRenderer, "プレイ時間: " + time, x + 5, y + delta, 0xffffffff);
                delta += 10;
            }

            // team
            if (showTeam) {
                if (showReward || showPlayTime) delta += 10;
                context.drawTextWithShadow(textRenderer, "生存者: " + KzeUtils.getSurvivorCount() + "人", x + 5, y + delta, 0xffffffff);
                delta += 10;
                context.drawTextWithShadow(textRenderer, "ゾンビ: " + KzeUtils.getZombieCount() + "人", x + 5, y + delta, 0xffffffff);

                delta += 10;
                if (showBonus) delta += 10;
            }

            // bonus
            if (showBonus) {
                if ((showReward || showPlayTime) && !showTeam) delta += 10;
                context.drawTextWithShadow(textRenderer, "防衛: " + Math.min(defenseBonus, 10000) + "円 (" + mainDefenseCount + ", " + subDefenseCount + ")", x + 5, y + delta, 0xffffffff);
                delta += 10;
                context.drawTextWithShadow(textRenderer, "被弾: " + Math.min(hitBonus, 4000) + "円 (" + hitCount + ")", x + 5, y + delta, 0xffffffff);
                delta += 10;
            }

            height = delta + 5;
        }

        @Override
        public int getWidth() {
            return 125;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public boolean isVisible() {
            return showReward || showPlayTime || showTeam || showBonus;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }
}
