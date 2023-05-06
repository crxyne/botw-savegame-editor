package org.crayne.bcse;

import org.crayne.bcse.parsed.ParsedSaveFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final ParsedSaveFile anotherTest = new ParsedSaveFile(new File("game_data.sav"));
        anotherTest.load();
    }

}