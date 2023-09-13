package net.toshimichi.kzeplus.context.widget;

import lombok.Data;
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
        double parentX = parent == null ? 0 : parent.getAbsoluteX();
        double parentY = parent == null ? 0 : parent.getAbsoluteY();
        double parentW = parent == null ? 0 : parent.getWidget().getWidth();
        double parentH = parent == null ? 0 : parent.getWidget().getHeight();

        absoluteX = (int) (x + parentX + parentW * anchor.getX() - widget.getWidth() * origin.getX());
        absoluteY = (int) (y + parentY + parentH * anchor.getY() - widget.getHeight() * origin.getY());

        children.forEach(child -> child.relocate(this, placeholder));
    }

    public void render(MatrixStack stack, float tickDelta) {
            widget.render(absoluteX, absoluteY, stack, tickDelta);
        children.forEach((child) -> child.render(stack, tickDelta));
    }
}
