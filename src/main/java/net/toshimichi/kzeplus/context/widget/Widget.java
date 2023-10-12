package net.toshimichi.kzeplus.context.widget;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public interface Widget {

    void update(boolean placeholder);

    void render(int x, int y, MatrixStack stack, float tickDelta);

    int getWidth();

    int getHeight();

    boolean isVisible();

    List<GameOptions> getOptions();
}
