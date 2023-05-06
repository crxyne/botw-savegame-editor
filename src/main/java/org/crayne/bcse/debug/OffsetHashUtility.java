package org.crayne.bcse.debug;

import org.crayne.bcse.parsed.ParsedSaveFile;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

import java.io.File;

public class OffsetHashUtility {

    // utility for finding where and how exactly certain information is stored
    private final ParsedSaveFile before;
    private final ParsedSaveFile after;

    public OffsetHashUtility(@NotNull final File before, @NotNull final File after) {
        this.before = new ParsedSaveFile(before);
        this.after = new ParsedSaveFile(after);

        this.before.load();
        this.after.load();
    }

    public void compare() {
        for (int i = 0x0; i < before.saveFile().buffer().array().length; i += 8) {
            final UInteger afterUint = after.saveFile().readUint32(i);
            final UInteger beforeUint = before.saveFile().readUint32(i);
            if (afterUint.equals(beforeUint)) continue;

            final float afterFloat = after.saveFile().readFloat32(i);
            final float beforeFloat = before.saveFile().readFloat32(i);

            System.out.println("difference at offset " + i + ": ");
            System.out.println(beforeUint + " / " + afterUint);
            System.out.println(Integer.toBinaryString(beforeUint.intValue()) + " / \n" + Integer.toBinaryString(afterUint.intValue()));
            System.out.println(Integer.toHexString(beforeUint.intValue()) + " / " + Integer.toHexString(afterUint.intValue()));
            System.out.println(beforeFloat + " / " + afterFloat);

            System.out.println("possible hash 1: 0x" + Integer.toHexString(before.saveFile().readUint32(i - 4).intValue()));
            System.out.println("possible hash 2: 0x" + Integer.toHexString(before.saveFile().readUint32(i - 4).intValue()));

            System.out.println("-".repeat(100));
        }
    }

    @NotNull
    public ParsedSaveFile after() {
        return after;
    }

    @NotNull
    public ParsedSaveFile before() {
        return before;
    }

}
