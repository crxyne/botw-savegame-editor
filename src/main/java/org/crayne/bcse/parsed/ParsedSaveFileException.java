package org.crayne.bcse.parsed;

import org.jetbrains.annotations.NotNull;

public class ParsedSaveFileException extends RuntimeException {

    public ParsedSaveFileException(@NotNull final Exception e) {
        super(e);
    }

    public ParsedSaveFileException(@NotNull final String s) {
        super(s);
    }

    public ParsedSaveFileException() {super();}

}
