package org.crayne.bcse;

import org.crayne.bcse.parsed.ParsedSaveFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final ParsedSaveFile saveFile = new ParsedSaveFile(new File("game_data.sav"));
        saveFile.load();

        saveFile.maxStamina(2.5f);

        saveFile.saveToFile(new File("game_data.sav"));
    }

}