package net.toshimichi.kzeplus.context.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GameContextRegistry {

    private static final Path SAVE_DIR = Path.of("./kze_plus/games");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<GameContext> gameContextHistories = new ArrayList<>();
    private GameContext currentGameContext;

    private void save() {
        if (!currentGameContext.isEnded()) throw new IllegalStateException("Game is not ended yet.");
        Path path = SAVE_DIR.resolve(currentGameContext.getStartedAt() + ".json");
        try {
            Files.createDirectories(SAVE_DIR);
            Files.writeString(path, GSON.toJson(currentGameContext));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGameContext() {
        currentGameContext = new GameContext(System.currentTimeMillis());
    }

    public void endGameContext() {
        if (currentGameContext == null) return;

        gameContextHistories.add(currentGameContext);
        save();

        currentGameContext = null;
    }
}
