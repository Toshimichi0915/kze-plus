package net.toshimichi.kzeplus.modules;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.widget.Widget;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class VoteInfoModule implements Module {

    private static final Gson GSON = new Gson();
    private static final TypeToken<List<VoteData>> TYPE_TOKEN = new TypeToken<>() {};
    private static final int PING_INTERVAL = 30000;

    private int nextVoteTicks;
    private boolean successful;
    private Thread thread;

    public void heartbeat() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ping();
                    successful = true;
                } catch (IOException e) {
                    successful = false;
                    e.printStackTrace();
                } finally {
                    Thread.sleep(PING_INTERVAL);
                }
            }
        } catch (InterruptedException e) {
            // module has been disabled
        }
    }

    public void ping() throws IOException {
        String username = MinecraftClient.getInstance().getSession().getUsername();
        URLConnection co = new URL("https://kze.toshimichi.net/votes?username=" + username).openConnection();

        String content;
        try (InputStream in = co.getInputStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<VoteData> voteData = GSON.fromJson(content, TYPE_TOKEN.getType());
        nextVoteTicks = (int) voteData
                .stream()
                .map(it -> Instant.parse(it.getCreatedAt()))
                .map(it -> Duration.between(Instant.now(), it.plus(Duration.ofDays(1))))
                .mapToLong(Duration::getSeconds)
                .map(it -> it * 20)
                .filter(it -> it > 0)
                .max()
                .orElse(0);
    }

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
        thread = new Thread(this::heartbeat);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        thread.interrupt();
    }

    @EventTarget
    public void reduceNextVoteTicks(ClientTickEvent e) {
        nextVoteTicks--;
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return Map.of("vote_info", new VoteInfoWidget());
    }

    @Data
    private static class VoteData {
        String username;
        String createdAt;
    }

    private class VoteInfoWidget implements Widget {

        private boolean valid;
        private boolean canVote;
        private String message;

        @Override
        public void update(boolean placeholder) {
            this.valid = successful && KzePlus.getInstance().getOptions().isShowVote() || placeholder;
            if (placeholder) {
                canVote = true;
                message = "投票できます";
            } else if (nextVoteTicks > 0) {
                canVote = false;
                int hours = nextVoteTicks / 20 / (60 * 60);
                int minutes = nextVoteTicks / 20 / 60 % 60;
                int seconds = nextVoteTicks / 20 % 60;
                message = String.format("投票可能まで: %02d:%02d:%02d", hours, minutes, seconds);
            } else {
                canVote = true;
                message = "投票できます";
            }
        }

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);
            InGameHud.drawTextWithShadow(stack, textRenderer, message, x + 5, y + 5, canVote ? 0x54fb54 : 0xffffff);
        }

        @Override
        public int getWidth() {
            return 125;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public boolean isVisible() {
            return valid;
        }

        @Override
        public List<GameOptions> getOptions() {
            return null;
        }
    }
}
