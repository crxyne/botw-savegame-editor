package org.crayne.bcse.internal;

import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

public enum SaveFileOffsetHash {

    RUPEES(0x23149bf8),
    MONS(0xce7afed3),
    MAX_HEARTS(0x2906f327),
    RELIC_GERUDO_AMOUNT(0x97f925c3),
    RELIC_GORON_AMOUNT(0xf1cf4807),
    RELIC_RITO_AMOUNT(0xfda0cde4),
    KOROK_SEED_AMOUNT(0x8a94e07a),
    DEFEATED_HINOX_AMOUNT(0x54679940),
    DEFEATED_TALUS_AMOUNT(0x698266be),
    DEFEATED_MOLDUGA_AMOUNT(0x441b7231),
    TIME_PLAYED(0x73c29681),
    HAS_MOTORCYCLE(0xc9328299, false),
    MAX_STAMINA(0x3adff047),
    MAP_NAME(0x0bee9e46),
    MAP_TYPE(0xd913b769),
    PLAYER_POSITION(0xa40ba103),
    HORSE_POSITION(0x982ba201),

    BOW_MODIFIER_TYPES(0x0cbf052a),
    SHIELD_MODIFIER_TYPES(0xc5238d2b),
    WEAPON_MODIFIER_TYPES(0x57ee221d),

    BOW_MODIFIER_VALUES(0x1e3fd294),
    SHIELD_MODIFIER_VALUES(0x69f17e8a),
    WEAPON_MODIFIER_VALUES(0xa6d926bc),
    ITEMS(0x5f283289),
    ITEMS_VALUE(0x6a09fc59), // for materials this is the quantity, for weapons the durability, etc

    ITEMS_EQUIPPED(0x824892be),

    HORSE_SADDLES(0x333aa6e5),
    HORSE_REINS(0x6150c6be),
    HORSE_NAMES(0x7b74e117),
    HORSE_MANES(0x9c6cfd3f),
    HORSE_TYPES(0xc247b696),
    HORSE_BONDS(0xe1a0ca54),
    MAP_ICON_POS(0xea9def3f),
    MAP_ICON_NO(0x9383490e),
    SELECTED_RUNE(0xa439d800);

    private final int offsetHash;
    private final boolean mustExist;

    SaveFileOffsetHash(final int offsetHash, final boolean mustExist) {
        this.offsetHash = offsetHash;
        this.mustExist = mustExist;
    }

    SaveFileOffsetHash(final int offsetHash) {
        this.offsetHash = offsetHash;
        this.mustExist = true;
    }

    @NotNull
    public UInteger offsetHash() {
        return UInteger.valueOf(offsetHash);
    }

    public boolean mustExist() {
        return mustExist;
    }
}
