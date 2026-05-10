package net.toshimichi.kzeplus.context.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

import java.util.List;

public interface Widget {

    void update(boolean placeholder);

    void render(int x, int y, DrawContext context, float tickDelta);

    int getWidth();

    int getHeight();

    boolean isVisible();

    List<GameOptions> getOptions();
}
