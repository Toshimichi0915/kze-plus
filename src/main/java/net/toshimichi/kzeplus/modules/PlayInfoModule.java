package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
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
        private boolean showExp;
        private boolean showPlayTime;
        private boolean showTeam;
        private boolean showBonus;

        @Override
        public void update(boolean placeholder) {
            showReward = KzePlus.getInstance().getOptions().isShowReward();
            showExp = KzePlus.getInstance().getOptions().isShowExp();
            showPlayTime = KzePlus.getInstance().getOptions().isShowPlayTime();
            showTeam = KzePlus.getInstance().getOptions().isShowTeam();
            showBonus = KzePlus.getInstance().getOptions().isShowBonus();
        }

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            if (!enabled) return;
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

            if (!showReward && !showExp && !showPlayTime && !showTeam && !showBonus) {
                height = 0;
                return;
            }
            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);

            int delta = 5;


            // reward
            if (showReward) {
                InGameHud.drawTextWithShadow(stack, textRenderer, "取得金額: " + reward + "円", x + 5, y + delta, 0xffffff);
                delta += 10;
            }

            // exp
            if (showExp) {
                InGameHud.drawTextWithShadow(stack, textRenderer, "取得経験値: " + exp, x + 5, y + delta, 0xffffff);
                delta += 10;
            }

            // play time
            if (showPlayTime) {
                Duration duration = Duration.ofSeconds(playTime / 20);
                String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
                InGameHud.drawTextWithShadow(stack, textRenderer, "プレイ時間: " + time, x + 5, y + delta, 0xffffff);
                delta += 10;
            }

            // team
            if (showTeam) {
                if (showReward || showExp || showPlayTime) delta += 10;
                InGameHud.drawTextWithShadow(stack, textRenderer, "生存者: " + KzeUtils.getSurvivorCount() + "人", x + 5, y + delta, 0xffffff);
                delta += 10;
                InGameHud.drawTextWithShadow(stack, textRenderer, "ゾンビ: " + KzeUtils.getZombieCount() + "人", y + 5, y + delta, 0xffffff);

                delta += 10;
                if (showBonus) delta += 10;
            }

            // bonus
            if (showBonus) {
                if ((showReward || showExp || showPlayTime) && !showTeam) delta += 10;
                InGameHud.drawTextWithShadow(stack, textRenderer, "防衛: " + Math.min(defenseBonus, 10000) + "円 (" + mainDefenseCount + ", " + subDefenseCount + ")", x + 5, y + delta, 0xffffff);
                delta += 10;
                InGameHud.drawTextWithShadow(stack, textRenderer, "被弾: " + Math.min(hitBonus, 4000) + "円 (" + hitCount + ")", x + 5, y + delta, 0xffffff);
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
            // TODO better margin calculation
            if (!showReward && !showExp && !showPlayTime && !showTeam && !showBonus) return -5;
            return height;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }
}
