package net.toshimichi.kzeplus;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.toshimichi.kzeplus.event.ClientTickEvent;
import net.toshimichi.kzeplus.event.EventRegistry;
import net.toshimichi.kzeplus.event.EventTarget;
import net.toshimichi.kzeplus.event.SimpleEventRegistry;
import net.toshimichi.kzeplus.module.InfoModule;
import net.toshimichi.kzeplus.module.Module;
import net.toshimichi.kzeplus.module.TimerModule;
import net.toshimichi.kzeplus.options.KzeOptions;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KzePlusMod implements ModInitializer {

    private static final Path CONFIG_PATH = Path.of("./kzeplus.json");
    private static final Gson GSON = new Gson();

    @Getter
    private static KzePlusMod instance;

    @Getter
    private KzeOptions options;

    @Getter
    private EventRegistry eventRegistry;

    private final List<Module> modules = new ArrayList<>();
    private boolean prevInKze;

    @Override
    public void onInitialize() {
        instance = this;

        // load options
        if (Files.exists(CONFIG_PATH)) {
            try {
                options = GSON.fromJson(Files.readString(CONFIG_PATH), KzeOptions.class);
            } catch (IOException | JsonSyntaxException e) {
                // create new options
            }
        }

        if (options == null) {
            options = new KzeOptions();
        }

        // set up event registry
        eventRegistry = new SimpleEventRegistry();
        eventRegistry.register(this);

        // set up modules
        modules.add(new InfoModule());
        modules.add(new TimerModule());
    }

    public void saveOptions() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(options));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventTarget
    private void toggleModules(ClientTickEvent e) {
        boolean inKze = KzeUtils.isInKze();
        if (prevInKze == inKze) return;
        prevInKze = inKze;

        if (inKze) {
            modules.forEach(Module::onEnable);
        } else {
            modules.forEach(Module::onDisable);
        }
    }
}
