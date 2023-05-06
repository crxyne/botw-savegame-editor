package org.crayne.bcse;

import org.crayne.bcse.debug.OffsetHashUtility;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final OffsetHashUtility offsetHashUtility = new OffsetHashUtility(new File("game_data_before.sav"), new File("game_data_after.sav"));
        offsetHashUtility.compare();
    }

}