package org.crayne.bcse.debug;

import org.crayne.bcse.parsed.ParsedSaveFile;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

import java.io.File;

public class OffsetHashUtility {

    // utility for finding where and how exactly certain information is stored
    private final File compareBefore;
    private final File compareAfter;

    public OffsetHashUtility(@NotNull final File compareBefore, @NotNull final File compareAfter) {
        this.compareBefore = compareBefore;
        this.compareAfter = compareAfter;
    }

    public void compare() {
        final ParsedSaveFile before = new ParsedSaveFile(compareBefore);
        before.load();

        final ParsedSaveFile after = new ParsedSaveFile(compareAfter);
        after.load();

        for (int i = 0x0; i < before.saveFile().buffer().array().length; i += 8) {
            final UInteger afterUint = after.saveFile().readUint32(i);
            final UInteger beforeUint = before.saveFile().readUint32(i);
            if (afterUint.equals(beforeUint)) continue;

            System.out.println("difference at offset " + i + ": " + beforeUint + " / " + afterUint);
            System.out.println("possible hash 1: 0x" + Integer.toHexString(before.saveFile().readUint32(i - 4).intValue()));
            System.out.println("possible hash 2: 0x" + Integer.toHexString(before.saveFile().readUint32(i - 4).intValue()));
        }
    }

    @NotNull
    public File compareAfter() {
        return compareAfter;
    }

    @NotNull
    public File compareBefore() {
        return compareBefore;
    }

}
