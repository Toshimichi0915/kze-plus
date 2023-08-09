package net.toshimichi.kzeplus.context.weapon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WeaponRegistry {

    private static final Path SAVE_DIR = Path.of("./kze_plus/weapons");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, WeaponInfo> weapons = new HashMap<>();

    public WeaponInfo load(String name) {
        WeaponInfo info = weapons.get(name);
        if (info != null) return info;

        try {
            Path path = SAVE_DIR.resolve(name + ".json");
            if (!path.toFile().exists()) return null;

            info = GSON.fromJson(Files.readString(path), WeaponInfo.class);
            weapons.put(name, info);
            return info;
        } catch (IOException e) {
            return null;
        }
    }

    public void save(WeaponInfo info) {
        try {
            Path path = SAVE_DIR.resolve(info.getName() + ".json");
            Files.createDirectories(SAVE_DIR);
            Files.writeString(path, GSON.toJson(info));
            weapons.put(info.getName(), info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
