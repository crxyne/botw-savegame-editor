package org.crayne.bcse;

import org.crayne.bcse.game.inventory.Inventory;
import org.crayne.bcse.game.item.other.MaterialItem;
import org.crayne.bcse.game.item.weapon.Modifier;
import org.crayne.bcse.game.item.weapon.ModifierType;
import org.crayne.bcse.parsed.ParsedSaveFile;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        // testing by making the entire inventory overpowered instantly

        final File save = new File("game_data.sav");
        final ParsedSaveFile saveFile = new ParsedSaveFile(save);
        saveFile.load();

        final Inventory inventory = saveFile.inventory().orElseThrow(RuntimeException::new);
        inventory.loadFromSavefile();

        // for some reason botw doesnt accept the uint max and resets it to 0?
        inventory.weapons().forEach(i -> {
            i.durability(Integer.MAX_VALUE);
            i.modifier(new Modifier(ModifierType.values(), true, UInteger.valueOf(Integer.MAX_VALUE)));
        });
        inventory.items()
                .stream()
                .filter(i -> i instanceof MaterialItem)
                .map(i -> (MaterialItem) i)
                .forEach(i -> i.quantity(Integer.MAX_VALUE));

        saveFile.rupees(UInteger.valueOf(Integer.MAX_VALUE));
        saveFile.maxHearts(UInteger.valueOf(0));

        saveFile.saveToFile(new File("game_data_modified.sav"));
    }

}