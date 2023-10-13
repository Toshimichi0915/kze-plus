package net.toshimichi.kzeplus.modules;

import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.widget.Widget;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.utils.GameRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillLogModule implements Module {

    private static final Pattern MAGAZINE_MESSAGE_PATTERN = Pattern.compile("^》 [^ ]+? 弾が切れた！$");
    private static final Pattern KILL_MESSAGE_PATTERN = Pattern.compile("^》(?:FirstBlood! )?([^ ]+?) killed by ([^ ]+?) \\(([^ ]+?) ?\\)$");
    private static final int MAX_KILL_LOGS = 5;
    private static final int KILL_LOG_DURATION = 200;

    private final List<KillLog> killLogs = new ArrayList<>();
    private boolean enabled;

    @Override
    public void onEnable() {
        enabled = true;
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        enabled = false;
        KzePlus.getInstance().getEventRegistry().unregister(this);
        killLogs.clear();
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return Map.of("kill_log", new KillLogWidget());
    }

    @EventTarget
    private void hideMagazineMessage(ChatEvent e) {
        String text = e.getText().getString();
        if (!KzePlus.getInstance().getOptions().isHideMagazineMessage()) return;
        if (!MAGAZINE_MESSAGE_PATTERN.matcher(text).find()) return;
        e.setCancelled(true);
    }

    @EventTarget
    private void hideKillMessage(ChatEvent e) {
        String text = e.getText().getString();
        if (!KzePlus.getInstance().getOptions().isHideKillMessage()) return;

        Matcher matcher = KILL_MESSAGE_PATTERN.matcher(text);
        if (!matcher.find()) return;
        e.setCancelled(true);

        String victim = matcher.group(1);
        GameRole victimRole = GameRole.fromName(victim);
        if (victimRole == null) victimRole = GameRole.SURVIVOR;

        String killer = matcher.group(2);
        GameRole killerRole = GameRole.fromName(killer);
        if (killerRole == null) killerRole = GameRole.SURVIVOR;

        String weapon = matcher.group(3);
        killLogs.add(new KillLog(killer, killerRole, victim, victimRole, weapon));

        if (killLogs.size() > MAX_KILL_LOGS) {
            killLogs.remove(0);
        }
    }

    @EventTarget
    private void removeKillLog(ClientTickEvent e) {
        // 1 tick = 50ms
        killLogs.removeIf(killLog -> System.currentTimeMillis() - killLog.getCreatedAt() > KILL_LOG_DURATION * 50);
    }

    @Data
    private static class KillLog {

        private static final int WEAPON_COLOR = 0xfc5454;
        private static final int SELF_COLOR = 0xffff55;

        private final String killer;
        private final GameRole killerRole;
        private final String victim;
        private final GameRole victimRole;
        private final String weapon;
        private final long createdAt = System.currentTimeMillis();

        public Text toText() {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            boolean didKill = player != null && killer.equals(player.getEntityName());
            boolean wasKilled = player != null && victim.equals(player.getEntityName());

            return Text.literal(killer).styled(style -> style.withColor(didKill ? SELF_COLOR : killerRole.getColor()))
                    .append(Text.literal(" -> ").formatted(Formatting.WHITE))
                    .append(Text.literal(victim).styled(style -> style.withColor(wasKilled ? SELF_COLOR : victimRole.getColor())))
                    .append(Text.literal(" (" + weapon + ")").styled(style -> style.withColor(WEAPON_COLOR)));
        }
    }

    private class KillLogWidget implements Widget {

        private static final List<KillLog> example = List.of(
                new KillLog("Toshimichi0915", GameRole.SURVIVOR, "T0shimichi", GameRole.ZOMBIE, "M4A1"),
                new KillLog("tsrly", GameRole.ZOMBIE, "MysticsMerchant", GameRole.SURVIVOR, "infected")
        );

        private boolean valid;
        private List<KillLog> target;

        @Override
        public void update(boolean placeholder) {
            if (placeholder) {
                target = example;
                valid = true;
            } else {
                target = killLogs;
                valid = KzePlus.getInstance().getOptions().isShowKillLog() && !target.isEmpty();
            }
        }

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            if (MinecraftClient.getInstance().options.playerListKey.isPressed()) return;

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);

            for (int i = 0; i < target.size(); i++) {
                KillLog killLog = target.get(i);
                InGameHud.drawTextWithShadow(stack, textRenderer, killLog.toText(), x + 5, y + 5 + i * 10, 0xFFFFFF);
            }
        }

        @Override
        public int getWidth() {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            int maxTextWidth = 0;
            for (KillLog killLog : target) {
                int textWidth = textRenderer.getWidth(killLog.toText());
                if (textWidth > maxTextWidth) maxTextWidth = textWidth;
            }

            return maxTextWidth + 10;
        }

        @Override
        public int getHeight() {
            return target.size() * 10 + 10;
        }

        @Override
        public boolean isVisible() {
            return valid;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }
}
