package net.toshimichi.kzeplus.events;

import lombok.Data;
import net.minecraft.text.Text;

@Data
public class ChatEvent implements Event {

    private final Text text;
    private boolean cancelled;
}
