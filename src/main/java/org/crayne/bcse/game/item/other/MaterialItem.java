package org.crayne.bcse.game.item.other;

import org.crayne.bcse.game.item.Item;
import org.crayne.bcse.game.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

public class MaterialItem implements Item {

    @NotNull
    private ItemType type;

    @NotNull
    private String name;

    @NotNull
    private UInteger value; // quantity of the item in this case

    public MaterialItem(@NotNull final String name, @NotNull final UInteger quantity) {
        this.name = name;
        this.value = quantity;
        this.type = ItemType.ofName(name);
    }

    @NotNull
    public String name() {
        return name;
    }

    public void name(@NotNull final String name) {
        this.name = name;
        this.type = ItemType.ofName(name);
    }

    @NotNull
    public ItemType type() {
        return type;
    }

    @NotNull
    public UInteger value() {
        return value;
    }

    public void value(@NotNull final UInteger value) {
        this.value = value;
    }

    public int quantity() {
        return value.intValue();
    }

    public void quantity(final int value) {
        this.value = UInteger.valueOf(value);
    }

    @NotNull
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", quantity=" + value +
                '}';
    }

}
