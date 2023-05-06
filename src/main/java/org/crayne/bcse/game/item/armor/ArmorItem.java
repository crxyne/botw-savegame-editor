package org.crayne.bcse.game.item.armor;

import org.crayne.bcse.game.item.EquippableItem;
import org.crayne.bcse.game.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

public class ArmorItem implements EquippableItem {

    @NotNull
    private String name;

    @NotNull
    private ItemType type;

    @NotNull
    private UInteger value; // value is the armor color in this case

    private boolean equipped;

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public ItemType type() {
        return type;
    }

    public ArmorItem(@NotNull final String name, @NotNull final UInteger value, final boolean equipped) {
        this.name = name;
        this.value = value;
        this.type = ItemType.ofName(name);
        this.equipped = equipped;
    }

    public void name(@NotNull final String name) {
        this.name = name;
        this.type = ItemType.ofName(name);
    }

    @NotNull
    public UInteger value() {
        return value;
    }

    public void value(@NotNull final UInteger value) {
        this.value = value;
    }

    public int color() {
        return value.intValue();
    }

    public void color(final int color) {
        this.value = UInteger.valueOf(color);
    }

    @NotNull
    public ArmorColor armorColor() {
        return ArmorColor.ofColorValue(value.intValue());
    }

    public boolean equipped() {
        return equipped;
    }

    public void equipped(final boolean b) {
        this.equipped = b;
    }

    @NotNull
    public String toString() {
        return "ArmorItem{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", equipped=" + equipped +
                '}';
    }
}
