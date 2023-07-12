package net.toshimichi.kzeplus.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ChatEvent;
import net.toshimichi.kzeplus.event.ClientTickEvent;
import net.toshimichi.kzeplus.event.EventTarget;
import net.toshimichi.kzeplus.event.InGameHudRenderEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerInfoModule implements Module {

    private static final int TIMER_LIMIT = 300;
    private static final Pattern TIMER_PATTERN = Pattern.compile("(\\d+) *?(?:s|S|秒)");
    private final List<Timer> timers = new ArrayList<>();

    @Override
    public void onEnable() {
        KzePlusMod.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlusMod.getInstance().getEventRegistry().unregister(this);
        timers.clear();
    }

    public Timer getTimerByName(String name) {
        return timers.stream()
                .filter(timer -> timer.getName().equals(name))
                .findAny()
                .orElse(null);
    }

    @EventTarget
    private void renderTimers(InGameHudRenderEvent e) {
        if (timers.isEmpty()) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        InGameHud.fill(e.getMatrices(), 20, 140, 125, 150 + timers.size() * 10, 0x80000000);
        for (Timer timer : timers) {
            InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, timer.toString(), 25, 145 + timers.indexOf(timer) * 10, 0xFFFFFF);
        }
    }

    @EventTarget
    private void addTimer(ChatEvent e) {
        String text = e.getText().getString();

        Matcher matcher = TIMER_PATTERN.matcher(text);
        if (!matcher.find()) return;
        int seconds = Integer.parseInt(matcher.group(1));
        if (seconds > TIMER_LIMIT) return; // prevent abuse

        // get timer name
        int index = 1;
        String name;
        do {
            name = "タイマー " + index++;
        } while (getTimerByName(name) != null);

        // register a new timer
        Timer timer = new Timer(name, seconds * 20);
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
