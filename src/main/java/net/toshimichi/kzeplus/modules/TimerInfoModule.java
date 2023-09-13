package net.toshimichi.kzeplus.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.widget.Widget;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerInfoModule implements Module {

    private static final int TIMER_LIMIT = 300;
    private static final Pattern CHAT_PATTERN = Pattern.compile("^([^ ]+?)(?: 》|:) .*$");
    private static final char[] ALT_NUMBERS = "０１２３４５６７８９".toCharArray();
    private static final Pattern TIMER_PATTERN = Pattern.compile("([\\d０１２３４５６７８９]+?) *?[sS秒]");
    private final List<Timer> timers = new ArrayList<>();
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
        timers.clear();
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return Map.of("timer_info", new TimerInfoWidget());
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
    private void addTimer(ChatEvent e) {
        String text = e.getText().getString();
        Matcher matcher = CHAT_PATTERN.matcher(text);
        if (matcher.find()) {
            String name = matcher.group(1);
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (networkHandler != null && networkHandler.getPlayerListEntry(name) != null) return;
        }

        text = stripPlayerName(text);
        if (text.contains("さんが参加しました")) return;
        if (text.contains("さんが投票をし")) return;

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

    private class TimerInfoWidget implements Widget {

        private static final List<Timer> example = List.of(
                new Timer("タイマー 1", 120),
                new Timer("タイマー 2", 230)
        );

        private boolean valid;
        private List<Timer> target;

        @Override
        public void update(boolean placeholder) {
            if (placeholder) {
                valid = true;
                target = example;
            } else {
                valid = enabled && !timers.isEmpty() && KzePlus.getInstance().getOptions().isShowTimer();
                target = timers;
            }
        }

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            if (!valid) return;

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);
            for (Timer timer : target) {
                InGameHud.drawTextWithShadow(stack, textRenderer, timer.toString(), x + 5, y + 5 + target.indexOf(timer) * 10, 0xFFFFFF);
            }
        }

        @Override
        public int getWidth() {
            return 125;
        }

        @Override
        public int getHeight() {
            if (!valid) return 0;
            return target.size() * 10 + 10;
        }

        @Override
        public List<GameOptions> getOptions() {
            return null;
        }
    }
}
