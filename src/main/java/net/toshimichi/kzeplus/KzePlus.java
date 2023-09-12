package net.toshimichi.kzeplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.option.KeyBinding;
import net.toshimichi.kzeplus.context.game.GameContextRegistry;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.weapon.WeaponRegistry;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventRegistry;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.SimpleEventRegistry;
import net.toshimichi.kzeplus.modules.GameContextModule;
import net.toshimichi.kzeplus.modules.KillLogModule;
import net.toshimichi.kzeplus.modules.Module;
import net.toshimichi.kzeplus.modules.PlayInfoModule;
import net.toshimichi.kzeplus.modules.TimerInfoModule;
import net.toshimichi.kzeplus.modules.VisibiiltyToggleModule;
import net.toshimichi.kzeplus.modules.WeaponContextModule;
import net.toshimichi.kzeplus.modules.WeaponInfoModule;
import net.toshimichi.kzeplus.options.KzeOptions;
import net.toshimichi.kzeplus.options.VisibilityMode;
import net.toshimichi.kzeplus.utils.KzeUtils;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KzePlus implements ModInitializer {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("./kzeplus.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String KZE_CATEGORY = "kze_plus.key.categories.kze_plus";
    public static final KeyBinding VISIBILITY_TOGGLE_KEY = new KeyBinding("kze_plus.key.toggle_visibility", GLFW.GLFW_KEY_V, KZE_CATEGORY);

    @Getter private static KzePlus instance;
    @Getter private ModContainer modContainer;
    @Getter private KzeOptions options;
    @Getter private EventRegistry eventRegistry;
    @Getter private WeaponRegistry weaponRegistry;
    @Getter private GameContextRegistry gameContextRegistry;
    @Getter private WeaponContext mainWeaponContext;
    @Getter private WeaponContext subWeaponContext;

    @Getter private final List<String> gunShotSounds = new ArrayList<>();
    @Getter @Setter private VisibilityMode defaultVisibility = VisibilityMode.FULL;

    private final List<Module> modules = new ArrayList<>();
    private boolean prevInKze;

    @Override
    public void onInitialize() {
        instance = this;
        modContainer = FabricLoader.getInstance().getModContainer("kze_plus").orElseThrow();

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

        // set up registries
        eventRegistry = new SimpleEventRegistry();
        eventRegistry.register(this);
        weaponRegistry = new WeaponRegistry();
        gameContextRegistry = new GameContextRegistry();

        // set up contexts
        mainWeaponContext = new WeaponContext(0);
        subWeaponContext = new WeaponContext(1);

        // register modules
        modules.add(new GameContextModule());
        modules.add(new PlayInfoModule());
        modules.add(new TimerInfoModule());
        modules.add(new KillLogModule());
        modules.add(new VisibiiltyToggleModule());
        modules.add(new WeaponContextModule());
        modules.add(new WeaponInfoModule());
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
