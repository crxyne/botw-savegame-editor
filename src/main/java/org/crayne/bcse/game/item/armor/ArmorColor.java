package org.crayne.bcse.game.item.armor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum ArmorColor {

    DEFAULT(0x0),
    BLUE(0x1),
    RED(0x2),
    YELLOW(0x3),
    WHITE(0x4),
    BLACK(0x5),
    PURPLE(0x6),
    GREEN(0x7),
    LIGHT_BLUE(0x8),
    NAVY(0x9),
    ORANGE(0xA),
    PEACH(0xB),
    CRIMSON(0xC),
    LIGHT_YELLOW(0xD),
    BROWN(0xE),
    GRAY(0xF),
    LOCKED(0xFFFFFFFF),
    UNKNOWN(0x10000000); // random value i chose, this is nothing special to worry about

    private final int colorValue;

    ArmorColor(final int colorValue) {
        this.colorValue = colorValue;
    }

    public int colorValue() {
        return colorValue;
    }

    @NotNull
    public static ArmorColor ofColorValue(final int colorValue) {
        return Arrays.stream(values()).filter(c -> c.colorValue == colorValue).findAny().orElse(UNKNOWN);
    }

    @NotNull
    public String toString() {
        return name().toLowerCase();
    }

}
