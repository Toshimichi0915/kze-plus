package net.toshimichi.kzeplus.context.widget;

import lombok.Data;

import java.util.List;

@Data
public class SerializedWidget {

    private final String id;
    private final Anchor anchor;
    private final Anchor origin;
    private final int x;
    private final int y;
    private final List<SerializedWidget> children;

    public static SerializedWidget serialize(WidgetLayout layout) {
        return new SerializedWidget(
                layout.getId(),
                layout.getAnchor(),
                layout.getOrigin(),
                layout.getX(),
                layout.getY(),
                layout.getChildren().stream().map(SerializedWidget::serialize).toList()
        );
    }
}
