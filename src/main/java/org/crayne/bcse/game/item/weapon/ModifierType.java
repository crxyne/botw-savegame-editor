package org.crayne.bcse.game.item.weapon;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum ModifierType {

    ATTACK_UP(0),
    DURABILITY(1),
    CRITICAL_HIT(2),
    LONG_THROW(3),
    MULTISHOT(4),
    ZOOM(5),
    QUICKSHOT(6),
    SURF_UP(7),
    GUARD_UP(8);

    private final int shift;

    ModifierType(final int shift) {
        this.shift = shift;
    }

    public int shift() {
        return shift;
    }

    public int binShifted() {
        return 1 << shift;
    }

    @NotNull
    public static Optional<ModifierType> ofShift(final int shift) {
        return Arrays.stream(values()).filter(t -> t.shift == shift).findAny();
    }

    @NotNull
    public String toString() {
        return name().toLowerCase();
    }

}
