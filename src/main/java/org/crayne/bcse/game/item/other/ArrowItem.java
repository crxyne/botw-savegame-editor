package org.crayne.bcse.game.item.other;

import org.crayne.bcse.game.item.EquippableItem;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

public class ArrowItem extends MaterialItem implements EquippableItem {

    private boolean equipped;

    public ArrowItem(@NotNull final String name, @NotNull final UInteger quantity, final boolean equipped) {
        super(name, quantity);
        this.equipped = equipped;
    }

    public boolean equipped() {
        return equipped;
    }

    public void equipped(final boolean b) {
        this.equipped = b;
    }

    @NotNull
    public String toString() {
        return "ArrowItem{" +
                "type=" + type() +
                ", name='" + name() + '\'' +
                ", quantity=" + value() +
                ", equipped=" + equipped +
                '}';
    }
}
