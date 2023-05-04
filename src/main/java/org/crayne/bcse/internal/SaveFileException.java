package org.crayne.bcse.internal;

import org.jetbrains.annotations.NotNull;

public class SaveFileException extends RuntimeException {

    public SaveFileException(@NotNull final Exception e) {
        super(e);
    }

    public SaveFileException(@NotNull final String s) {
        super(s);
    }

    public SaveFileException() {super();}

}
