package net.toshimichi.kzeplus.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GameRole {

    SURVIVOR("e", 0x00a8a8),
    ZOMBIE("z", 0x54fb54);

    private final String teamName;
    private final int color;
}
