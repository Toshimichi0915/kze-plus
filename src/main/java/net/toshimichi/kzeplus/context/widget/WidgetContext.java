package net.toshimichi.kzeplus.context.widget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class WidgetContext {

    private static final Identifier DEFAULT_LAYOUT = new Identifier("kze_plus", "default_layout.json");
    private static final Path SAVE_PATH = Path.of("./kze_plus/widgets.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Widget> unresolved = new HashMap<>();

    @Getter
    private final WidgetLayout root = new WidgetLayout("root", new ScreenWidget(), Anchor.TOP_LEFT, Anchor.TOP_LEFT, 0, 0);

    public void register(String id, Widget widget) {
        unresolved.put(id, widget);
    }

    public void save() {
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            Files.writeString(SAVE_PATH, GSON.toJson(SerializedWidget.serialize(root)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WidgetLayout resolve(SerializedWidget serialized) {
        Widget widget = unresolved.remove(serialized.getId());

        WidgetLayout layout;
        if (serialized.getId().equals("root")) {
            layout = root;
        } else if (widget != null) {
            layout = new WidgetLayout(serialized.getId(), widget, serialized.getAnchor(), serialized.getOrigin(), serialized.getX(), serialized.getY());
        } else {
            return null;
        }

        for (SerializedWidget child : serialized.getChildren()) {
            WidgetLayout childLayout = resolve(child);
            if (childLayout == null) continue;
            layout.getChildren().add(childLayout);
        }

        return layout;
    }

    public void flush() {
        SerializedWidget serializedRoot;

        try (InputStream in = MinecraftClient.getInstance().getResourceManager().open(DEFAULT_LAYOUT)) {
            serializedRoot = GSON.fromJson(new String(in.readAllBytes()), SerializedWidget.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            if (Files.exists(SAVE_PATH)) {
                serializedRoot = GSON.fromJson(Files.readString(SAVE_PATH), SerializedWidget.class);
            }
        } catch (IOException e) {
            // simply use the default
        }

        resolve(serializedRoot);

        for (Map.Entry<String, Widget> entry : unresolved.entrySet()) {
            WidgetLayout layout = new WidgetLayout(entry.getKey(), entry.getValue(), Anchor.TOP_LEFT, Anchor.TOP_LEFT, 0, 0);
            root.getChildren().add(layout);
        }
        unresolved.clear();
    }
}
