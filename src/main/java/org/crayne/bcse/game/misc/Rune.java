package org.crayne.bcse.game.misc;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum Rune {

    ROUND_REMOTE_BOMB(0),
    CUBE_REMOTE_BOMB(1),
    MAGNESIS(2),
    STASIS(3),
    CRYONIS(4),
    CAMERA(5),
    MASTER_CYCLE_ZERO(6),
    AMIIBO(7);

    private final int id;

    Rune(final int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    @NotNull
    public static Optional<Rune> ofId(final int id) {
        return Arrays.stream(values()).filter(r -> r.id == id).findAny();
    }

}
