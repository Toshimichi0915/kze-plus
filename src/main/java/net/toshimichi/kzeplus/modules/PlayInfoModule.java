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

        @Override
        public void update(boolean placeholder) {

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

            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);

            // reward
            InGameHud.drawTextWithShadow(stack, textRenderer, "取得金額: " + reward + "円", x + 5, y + 5, 0xffffff);

            // exp
            InGameHud.drawTextWithShadow(stack, textRenderer, "取得経験値: " + exp, x + 5, y + 15, 0xffffff);

            // play time
            Duration duration = Duration.ofSeconds(playTime / 20);
            String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
            InGameHud.drawTextWithShadow(stack, textRenderer, "プレイ時間: " + time, x + 5, x + 25, 0xffffff);

            InGameHud.drawTextWithShadow(stack, textRenderer, "生存者: " + KzeUtils.getSurvivorCount() + "人", x + 5, x + 45, 0xffffff);
            InGameHud.drawTextWithShadow(stack, textRenderer, "ゾンビ: " + KzeUtils.getZombieCount() + "人", y + 5, y + 55, 0xffffff);

            // bonus
            InGameHud.drawTextWithShadow(stack, textRenderer, "防衛: " + Math.min(defenseBonus, 10000) + "円 (" + mainDefenseCount + ", " + subDefenseCount + ")", x + 5, y + 75, 0xffffff);
            InGameHud.drawTextWithShadow(stack, textRenderer, "被弾: " + Math.min(hitBonus, 4000) + "円 (" + hitCount + ")", x + 5, y + 85, 0xffffff);
        }

        @Override
        public int getWidth() {
            return 125;
        }

        @Override
        public int getHeight() {
            return 100;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }
}
