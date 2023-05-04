package org.crayne.bcse.game.item;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum ItemType {

    ARROW,
    MATERIAL,
    MELEE,
    BOW,
    SHIELD,
    SPECIAL,
    ARMOR,
    UNCATEGORIZED;

    @NotNull
    private static final Set<String> SPECIAL_ITEM_ID_BEGINS = new HashSet<>(List.of(
            "Get_TwnObj_DLC", "GameRom", "KeySmall", "PlayerStole2", "Obj_"
    ));

    public boolean isWeapon() {
        return this == MELEE || this == SHIELD || this == BOW;
    }

    @NotNull
    public static ItemType ofName(@NotNull final String itemIdName) {
        if (itemIdName.endsWith("Arrow") || itemIdName.endsWith("Arrow_A")) return ARROW;
        // TODO make a proper translation system on top of this to add support for categorizing unconventional item ids in mods

        switch (Optional.ofNullable(StringUtils.substringBetween(itemIdName, "Weapon_", "_")).orElse(itemIdName)) {
            case "Sword", "Lsword", "Spear" -> {
                return MELEE;
            }
            case "Bow", "AncientBowMark" -> {
                return BOW;
            }
            case "Shield" -> {
                return SHIELD;
            }
        }

        if (itemIdName.startsWith("Armor_")) return ARMOR;
        if (SPECIAL_ITEM_ID_BEGINS.stream().anyMatch(itemIdName::startsWith) && !itemIdName.equals("Obj_FireWoodBundle"))
            return SPECIAL;
        return UNCATEGORIZED; // materials, food, or any modded item that didn't pass the above filters due to unconventional naming
        // TODO categorize materials and food separately using the translation system
    }

    @NotNull
    public String toString() {
        return name().toLowerCase();
    }

}
