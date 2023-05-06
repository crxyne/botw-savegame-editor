package org.crayne.bcse.game.item;

public interface EquippableItem extends Item {

    boolean equipped();
    void equipped(final boolean b);

}
