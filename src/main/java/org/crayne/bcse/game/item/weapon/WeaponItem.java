package org.crayne.bcse.game.item.weapon;

import org.crayne.bcse.game.item.Item;
import org.crayne.bcse.game.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joou.UInteger;

import java.util.Optional;

public class WeaponItem implements Item {

    @NotNull
    private String name;

    @NotNull
    private ItemType type;

    @NotNull
    private UInteger value; // the value is the durability in this case

    @Nullable
    private Modifier modifier;

    public WeaponItem(@NotNull final String name, @NotNull final UInteger durability, @Nullable final Modifier modifier) {
        this.name = name;
        this.type = ItemType.ofName(name);
        this.value = durability;
        this.modifier = modifier == null || (modifier.types().length == 0 && modifier.value().intValue() == 0 && !modifier.plus()) ? null : modifier;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public ItemType type() {
        return type;
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

    public int durability() {
        return value.intValue();
    }

    public void durability(final int durability) {
        this.value = UInteger.valueOf(durability);
    }

    @NotNull
    public Optional<Modifier> modifier() {
        return Optional.ofNullable(modifier);
    }

    public void modifier(@Nullable final Modifier modifier) {
        this.modifier = modifier;
    }

    @NotNull
    public String toString() {
        return "WeaponItem{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", durability=" + value +
                ", modifier=" + modifier +
                '}';
    }
}
