package org.crayne.bcse.game.item;

import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

public interface Item {

    @NotNull
    String name();

    void name(@NotNull final String name);

    @NotNull
    ItemType type();

    UInteger value();

    void value(@NotNull final UInteger value);

}
