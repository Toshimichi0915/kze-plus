package net.toshimichi.kzeplus.context.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class ScreenWidget implements Widget {

    @Override
    public void update(boolean placeholder) {

    }

    @Override
    public void render(int x, int y, MatrixStack stack, float tickDelta) {

    }

    private Window getWindow() {
        return MinecraftClient.getInstance().getWindow();
    }

    @Override
    public int getWidth() {
        return getWindow().getScaledWidth();
    }

    @Override
    public int getHeight() {
        return getWindow().getScaledHeight();
    }

    @Override
    public List<GameOptions> getOptions() {
        return List.of();
    }
}
