package net.toshimichi.kzeplus.modules;

import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import net.toshimichi.kzeplus.utils.GameRole;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillLogModules implements Module {

    private static final Pattern MAGAZINE_MESSAGE_PATTERN = Pattern.compile("^》[^ ]+? 弾が切れた！$");
    private static final Pattern KILL_MESSAGE_PATTERN = Pattern.compile("^》(?:FirstBlood! )?([^ ]+?) killed by ([^ ]+?) \\(([^ ]+?) ?\\)$");
    private static final int MAX_KILL_LOGS = 5;
    private static final int KILL_LOG_DURATION = 200;

    private final List<KillLog> killLogs = new ArrayList<>();

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        killLogs.clear();
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
        String killer = matcher.group(2);
        String weapon = matcher.group(3);
        killLogs.add(new KillLog(killer, victim, weapon));

        if (killLogs.size() > MAX_KILL_LOGS) {
            killLogs.remove(0);
        }
    }

    @EventTarget
    private void removeKillLog(ClientTickEvent e) {
        // 1 tick = 50ms
        killLogs.removeIf(killLog -> System.currentTimeMillis() - killLog.getCreatedAt() > KILL_LOG_DURATION * 50);
    }

    @EventTarget
    private void showKillLog(InGameHudRenderEvent e) {
        if (!KzePlus.getInstance().getOptions().isShowKillLog()) return;
        if (killLogs.isEmpty()) return;
        if (MinecraftClient.getInstance().options.playerListKey.isPressed()) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // get max text width
        int maxTextWidth = 0;
        for (KillLog killLog : killLogs) {
            int textWidth = textRenderer.getWidth(killLog.toText());
            if (textWidth > maxTextWidth) maxTextWidth = textWidth;
        }

        InGameHud.fill(e.getMatrices(), 135, 20, 145 + maxTextWidth, 30 + killLogs.size() * 10, 0x80000000);

        for (int i = 0; i < killLogs.size(); i++) {
            KillLog killLog = killLogs.get(i);
            InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, killLog.toText(), 140, 25 + i * 10, 0xFFFFFF);
        }
    }

    @Data
    private static class KillLog {

        private static final int WEAPON_COLOR = 0xfc5454;
        private static final int SELF_COLOR = 0xffff55;

        private final String killer;
        private final String victim;
        private final String weapon;
        private final long createdAt = System.currentTimeMillis();

        public Text toText() {
            boolean infected = weapon.equals("infected");
            GameRole killerRole = infected ? GameRole.ZOMBIE : GameRole.SURVIVOR;
            GameRole victimRole = !infected ? GameRole.ZOMBIE : GameRole.SURVIVOR;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            boolean didKill = player != null && killer.equals(player.getEntityName());
            boolean wasKilled = player != null && victim.equals(player.getEntityName());

            return Text.literal(killer).styled(style -> style.withColor(didKill ? SELF_COLOR : killerRole.getColor()))
                    .append(Text.literal(" -> ").formatted(Formatting.WHITE))
                    .append(Text.literal(victim).styled(style -> style.withColor(wasKilled ? SELF_COLOR : victimRole.getColor())))
                    .append(Text.literal(" (" + weapon + ")").styled(style -> style.withColor(WEAPON_COLOR)));
        }
    }
}
