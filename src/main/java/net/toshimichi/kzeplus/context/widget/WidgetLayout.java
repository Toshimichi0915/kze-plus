package net.toshimichi.kzeplus.context.widget;

import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Data
public class WidgetLayout {

    private final String id;
    private final Widget widget;
    private final Anchor anchor;
    private final Anchor origin;
    private final int x;
    private final int y;
    private final List<WidgetLayout> children = new ArrayList<>();

    // computed
    private int absoluteX;
    private int absoluteY;

    public void relocate(WidgetLayout parent, boolean placeholder) {
        widget.update(placeholder);

        Window window = MinecraftClient.getInstance().getWindow();

        int x, y;
        double parentX, parentY, parentWidth, parentHeight;
        if (parent == null) {
            x = this.x;
            y = this.y;
            parentX = 0;
            parentY = 0;
            parentWidth = window.getScaledWidth();
            parentHeight = window.getScaledHeight();
        } else {
            parentX = parent.getAbsoluteX();
            parentY = parent.getAbsoluteY();
            if (parent.getWidget().isVisible()) {
                x = this.x;
                y = this.y;
                parentWidth = parent.getWidget().getWidth();
                parentHeight = parent.getWidget().getHeight();
            } else {
                x = 0;
                y = 0;
                parentWidth = 0;
                parentHeight = 0;
            }
        }

        absoluteX = (int) (x + parentX + parentWidth * anchor.getX() - widget.getWidth() * origin.getX());
        absoluteY = (int) (y + parentY + parentHeight * anchor.getY() - widget.getHeight() * origin.getY());

        children.forEach(child -> child.relocate(this, placeholder));
    }

    public void render(MatrixStack stack, float tickDelta) {
        if (widget.isVisible()) {
            widget.render(absoluteX, absoluteY, stack, tickDelta);
        }

        children.forEach((child) -> child.render(stack, tickDelta));
    }
}
