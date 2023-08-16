package net.toshimichi.kzeplus.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerInfoModule implements Module {

    private static final int TIMER_LIMIT = 300;
    private static final Pattern CHAT_PATTERN = Pattern.compile("^([^ ]+?)(?: 》|:) .*$");
    private static final char[] ALT_NUMBERS = "０１２３４５６７８９".toCharArray();
    private static final Pattern TIMER_PATTERN = Pattern.compile("([\\d０１２３４５６７８９]+?) *?[sS秒]");
    private final List<Timer> timers = new ArrayList<>();

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        timers.clear();
    }

    public Timer getTimerByName(String name) {
        return timers.stream()
                .filter(timer -> timer.getName().equals(name))
                .findAny()
                .orElse(null);
    }

    private List<Timer> getTimerByTick(int min, int max) {
        return timers.stream()
                .filter(timer -> min <= timer.getRemainingTicks() && timer.getRemainingTicks() <= max)
                .toList();
    }

    private String stripPlayerName(String text) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return text;

        return player.networkHandler.getPlayerList()
                .stream()
                .map(entry -> entry.getProfile().getName())
                .reduce(text, (s, name) -> s.replace(name, ""));
    }

    @EventTarget
    private void renderTimers(InGameHudRenderEvent e) {
        if (!KzePlus.getInstance().getOptions().isShowTimer()) return;
        if (timers.isEmpty()) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        InGameHud.fill(e.getMatrices(), 20, 170, 145, 180 + timers.size() * 10, 0x80000000);
        for (Timer timer : timers) {
            InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, timer.toString(), 25, 175 + timers.indexOf(timer) * 10, 0xFFFFFF);
        }
    }

    @EventTarget
    private void addTimer(ChatEvent e) {
        String text = e.getText().getString();
        Matcher matcher = CHAT_PATTERN.matcher(text);
        if (matcher.find()) {
            String name = matcher.group(1);
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (networkHandler != null && networkHandler.getPlayerListEntry(name) != null) return;
        }

        text = stripPlayerName(text);
        if (text.contains("参加しました")) return;
        if (text.contains("投票しました")) return;

        matcher = TIMER_PATTERN.matcher(text);
        if (!matcher.find()) return;

        String number = matcher.group(1);
        for (int i = 0; i < ALT_NUMBERS.length; i++) {
            number = number.replace(ALT_NUMBERS[i], Character.forDigit(i, 10));
        }

        int seconds = Integer.parseInt(number);
        if (seconds > TIMER_LIMIT) return; // prevent abuse

        int ticks = seconds * 20;
        if (!getTimerByTick(ticks - 20, ticks + 20).isEmpty()) return;

        // get timer name
        int index = 1;
        String name;
        do {
            name = "タイマー " + index++;
        } while (getTimerByName(name) != null);

        // register a new timer
        Timer timer = new Timer(name, ticks);
        timers.add(timer);
    }

    @EventTarget
    private void tickTimer(ClientTickEvent e) {
        timers.removeIf(timer -> {
            timer.setRemainingTicks(timer.getRemainingTicks() - 1);
            return timer.getRemainingTicks() <= 0;
        });
    }

    @Data
    @AllArgsConstructor
    private static class Timer {

        private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

        private final String name;
        private int remainingTicks;

        @Override
        public String toString() {
            return name + ": " + FORMAT.format(remainingTicks / 20D) + "秒";
        }
    }
}
