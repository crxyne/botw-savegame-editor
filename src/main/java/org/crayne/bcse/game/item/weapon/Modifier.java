package org.crayne.bcse.game.item.weapon;

import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Modifier {

    @NotNull
    private ModifierType[] types;
    private boolean plus;

    @NotNull
    private UInteger value;

    public Modifier(@NotNull final ModifierType[] types, final boolean plus, @NotNull final UInteger value) {
        this.types = types;
        this.plus = plus;
        this.value = value;
    }

    public boolean plus() {
        return plus;
    }

    @NotNull
    public UInteger value() {
        return value;
    }

    @NotNull
    public ModifierType[] types() {
        return types;
    }

    @NotNull
    public static Modifier of(@NotNull final UInteger typeInt, @NotNull final UInteger modifValue) {
        final List<ModifierType> types = new ArrayList<>();
        final int typeIntValue = typeInt.intValue();
        for (@NotNull final ModifierType modifierType : ModifierType.values()) {
            final boolean hasModifier = (typeIntValue & (1 << modifierType.shift())) != 0;
            if (hasModifier) types.add(modifierType);
        }
        final boolean plus = (typeIntValue & (1 << 31)) != 0;
        return new Modifier(types.toArray(new ModifierType[0]), plus, modifValue);
    }

    @NotNull
    public UInteger typeInt() {
        final AtomicInteger typeInt = new AtomicInteger((plus ? 1 : 0) << 31);
        Arrays.stream(types).forEach(t -> typeInt.updateAndGet(v -> v | t.binShifted()));
        return UInteger.valueOf(typeInt.get());
    }

    public void plus(final boolean plus) {
        this.plus = plus;
    }

    public void type(@NotNull final ModifierType type, final boolean active) {
        final List<ModifierType> types = Arrays.asList(this.types);
        if (types.contains(type)) {
            if (!active) types.remove(type);
            return;
        }
        if (active) types.add(type);
        this.types = types.stream()
                .sorted(Comparator.comparing(ModifierType::shift))
                .toList()
                .toArray(new ModifierType[0]);
    }

    public void value(@NotNull final UInteger value) {
        this.value = value;
    }

    @NotNull
    public String toString() {
        final String binStr = Integer.toBinaryString(typeInt().intValue() & ~(1 << 31));

        return "Modifier{" +
                "types=" + Arrays.toString(types) +
                ", binary-types=" + "0".repeat(9 - binStr.length()) + binStr +
                ", plus=" + plus +
                ", value=" + value +
                '}';
    }

}
