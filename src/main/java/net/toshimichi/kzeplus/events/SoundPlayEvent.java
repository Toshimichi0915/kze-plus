package net.toshimichi.kzeplus.events;

import lombok.Data;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

@Data
public class SoundPlayEvent implements Event {

    private final double x;
    private final double y;
    private final double z;
    private final SoundEvent sound;
    private final SoundCategory category;
    private final float volume;
    private final float pitch;
    private final boolean useDistance;
    private final long seed;
}
