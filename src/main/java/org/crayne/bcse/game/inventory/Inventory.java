package org.crayne.bcse.game.inventory;

import org.apache.commons.lang3.function.TriFunction;
import org.crayne.bcse.game.item.EquippableItem;
import org.crayne.bcse.game.item.Item;
import org.crayne.bcse.game.item.ItemType;
import org.crayne.bcse.game.item.armor.ArmorItem;
import org.crayne.bcse.game.item.other.ArrowItem;
import org.crayne.bcse.game.item.other.MaterialItem;
import org.crayne.bcse.game.item.other.SpecialItem;
import org.crayne.bcse.game.item.weapon.Modifier;
import org.crayne.bcse.game.item.weapon.WeaponItem;
import org.crayne.bcse.internal.SaveFileOffsetHash;
import org.crayne.bcse.parsed.ParsedSaveFile;
import org.crayne.bcse.parsed.ParsedSaveFileException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joou.UInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Inventory {

    private final List<Item> items;

    @Nullable
    private ParsedSaveFile saveFile;

    public static final int MAX_ITEM_AMOUNT = 420;

    public Inventory() {
        this.saveFile = null;
        this.items = new ArrayList<>();
    }

    public Inventory(@Nullable final ParsedSaveFile saveFile) {
        this.saveFile = saveFile;
        this.items = new ArrayList<>();
    }

    public void reattachSavefile(@Nullable final ParsedSaveFile saveFile) {
        if (this.saveFile != null) this.saveFile.inventory(null);
        this.saveFile = saveFile;
        if (saveFile != null) saveFile.inventory(this);
    }

    @Nullable
    public ParsedSaveFile attachedSavefile() {
        return saveFile;
    }

    public void loadFromSavefile() {
        if (saveFile == null)
            throw new ParsedSaveFileException("Cannot load inventory; No savefile was loaded");

        items.clear();
        final int continueArmor = loadMeleeBowsShieldsArrows();
        final int continueMaterials = loadArmor(continueArmor);
        final int continueSpecials = loadMaterials(continueMaterials);
        final int inventorySize = loadSpecials(continueSpecials);
    }

    private <T extends Item> int loadAny(final int startAt, @NotNull final ItemType required, @NotNull final TriFunction<String, UInteger, Boolean, T> triFunction) {
        for (int i = startAt; i < MAX_ITEM_AMOUNT; i++) {
            final Optional<String> item = readItemName(i);
            if (item.isEmpty()) return i;

            final String itemName = item.get();
            final ItemType type = ItemType.ofName(itemName);
            if (loadAnySingle(type, i, required, itemName, triFunction)) continue;
            return i;
        }
        return MAX_ITEM_AMOUNT;
    }

    private <T extends Item> boolean loadAnySingle(@NotNull final ItemType type, final int slot, @NotNull final ItemType required,
                                                   @NotNull final String itemName, @NotNull final TriFunction<String, UInteger, Boolean, T> triFunction) {

        if (type != required) return false;

        final UInteger quantity = readItemValue(slot);
        final boolean equipped = readEquippedItem(slot).intValue() != 0;
        final T materialItem = triFunction.apply(itemName, quantity, equipped);
        items.add(materialItem);
        return true;
    }

    private int loadMaterials(final int startAt) {
        return loadAny(startAt, ItemType.UNCATEGORIZED, (name, slot, unused) -> new MaterialItem(name, slot));
    }

    private int loadSpecials(final int startAt) {
        return loadAny(startAt, ItemType.SPECIAL, (name, slot, unused) -> new SpecialItem(name, slot));
    }

    private int loadArmor(final int startAt) {
        return loadAny(startAt, ItemType.ARMOR, ArmorItem::new);
    }

    private int loadMeleeBowsShieldsArrows() {
        int meleeModifierindex = 0, bowModifierIndex = 0, shieldModifierIndex = 0;
        for (int i = 0; i < MAX_ITEM_AMOUNT; i++) {
            final Optional<String> item = readItemName(i);
            if (item.isEmpty()) return i;

            final String itemName = item.get();
            final ItemType type = ItemType.ofName(itemName);

            final Optional<Integer> modifierIndex = Optional.ofNullable(switch (type) {
                case MELEE -> meleeModifierindex++;
                case BOW -> bowModifierIndex++;
                case SHIELD -> shieldModifierIndex++;
                default -> null;
            });
            if (modifierIndex.isEmpty()) {
                if (maybeLoadArrow(type, itemName, i)) continue;
                return i; // we have finished loading weapons, next theres armor which is NOT a weapon, so return here
            }
            loadWeaponBowShield(itemName, i, modifierIndex.get());
        }
        return MAX_ITEM_AMOUNT;
    }

    public void saveToSavefile() {
        if (saveFile == null)
            throw new ParsedSaveFileException("Cannot save inventory; No savefile was loaded");
        if (items.size() > MAX_ITEM_AMOUNT)
            throw new ParsedSaveFileException("Cannot save inventory; Size is larger than the maximum (" + MAX_ITEM_AMOUNT + ")");

        final int startBowsAt = saveMelee();
        final int startArrowsAt = saveBows(startBowsAt);
        final int startShieldsAt = saveArrows(startArrowsAt);
        final int startArmorAt = saveShields(startShieldsAt);
        final int startMaterialsAt = saveArmor(startArmorAt);
        final int startSpecialsAt = saveUncategorizedMaterials(startMaterialsAt);
        final int startEmptySlotsAt = saveSpecials(startSpecialsAt);

        // this adds proper support for adding / removing / inserting items on the go
        for (int i = startEmptySlotsAt; i < MAX_ITEM_AMOUNT; i++) {
            saveEmptySlot(i);
        }
    }

    private int saveItems(final int startSlot, @Nullable final ItemType type, @NotNull final Predicate<Item> filter, @NotNull final BiConsumer<Item, Integer> consumer) {
        final AtomicInteger slot = new AtomicInteger(startSlot);

        items.stream()
                .filter(i -> type == null || i.type() == type)
                .filter(filter)
                .forEachOrdered(item -> {
                    consumer.accept(item, slot.get());
                    slot.getAndIncrement();
                });

        return slot.get();
    }

    private int saveWeapons(@NotNull final SaveFileOffsetHash modifierTypeHash, @NotNull final SaveFileOffsetHash modifierValueHash,
                             @NotNull final ItemType itemType, final int startSlot) {

        final AtomicInteger modifierIndex = new AtomicInteger();
        return saveItems(startSlot, itemType, item -> item instanceof WeaponItem,
                (item, slot) -> {
                    saveWeapon(modifierTypeHash, modifierValueHash, (WeaponItem) item, slot, modifierIndex.get());
                    modifierIndex.getAndIncrement();
                });
    }

    private int saveArmor(final int startSlot) {
        return saveItems(startSlot, ItemType.ARMOR, item -> item instanceof ArmorItem, this::saveItemEquipped);
    }

    private int saveUncategorizedMaterials(final int startSlot) {
        return saveMaterials(startSlot, ItemType.UNCATEGORIZED);
    }

    private int saveSpecials(final int startSlot) {
        return saveMaterials(startSlot, ItemType.SPECIAL);
    }

    private int saveArrows(final int startSlot) {
        return saveMaterials(startSlot, ItemType.ARROW);
    }

    private int saveMaterials(final int startSlot, @NotNull final ItemType type) {
        return saveItems(startSlot, type, item -> item instanceof MaterialItem, this::saveItem);
    }

    private void saveItem(@NotNull final Item item, final int slot) {
        final UInteger value = item.value();
        final String name = item.name();

        writeItemName(slot, name);
        writeItemValue(slot, value);
    }

    private void saveItemEquipped(@NotNull final Item item, final int slot) {
        final UInteger value = item.value();
        final String name = item.name();
        final UInteger equipped = UInteger.valueOf(((EquippableItem) item).equipped() ? 1 : 0);

        writeItemName(slot, name);
        writeItemValue(slot, value);
        writeEquippedItem(slot, equipped);
    }

    private void saveEmptySlot(final int slot) {
        writeItemName(slot, "\0".repeat(64));
    }

    private void saveWeapon(@NotNull final SaveFileOffsetHash modifierTypeHash, @NotNull final SaveFileOffsetHash modifierValueHash,
                            @NotNull final WeaponItem item, final int slot, final int modifierIndex) {

        final UInteger modifierType = item.modifier().map(Modifier::typeInt).orElse(UInteger.valueOf(0));
        final UInteger modifierValue = item.modifier().map(Modifier::value).orElse(UInteger.valueOf(0));
        final UInteger value = item.value();
        final String name = item.name();
        final UInteger equipped = UInteger.valueOf(item.equipped() ? 1 : 0);

        writeItemName(slot, name);
        writeItemValue(slot, value);
        writeWeaponModifierType(modifierTypeHash, modifierIndex, modifierType);
        writeWeaponModifierValue(modifierValueHash, modifierIndex, modifierValue);
        writeEquippedItem(slot, equipped);
    }

    private int saveMelee() {
        return saveMelee(0);
    }

    private int saveMelee(final int startSlot) {
        return saveWeapons(SaveFileOffsetHash.WEAPON_MODIFIER_TYPES, SaveFileOffsetHash.WEAPON_MODIFIER_VALUES, ItemType.MELEE, startSlot);
    }

    private int saveBows(final int startSlot) {
        return saveWeapons(SaveFileOffsetHash.BOW_MODIFIER_TYPES, SaveFileOffsetHash.BOW_MODIFIER_VALUES, ItemType.BOW, startSlot);
    }

    private int saveShields(final int startSlot) {
        return saveWeapons(SaveFileOffsetHash.SHIELD_MODIFIER_TYPES, SaveFileOffsetHash.SHIELD_MODIFIER_VALUES, ItemType.SHIELD, startSlot);
    }

    private boolean maybeLoadArrow(@NotNull final ItemType type, @NotNull final String itemName, final int slot) {
        return loadAnySingle(type, slot, ItemType.ARROW, itemName, (name, quantity, ignored) -> new ArrowItem(name, quantity));
    }

    private void loadWeaponBowShield(@NotNull final String name, final int slot, final int modifierIndex) {
        final UInteger durability = readItemValue(slot);
        final boolean equipped = readEquippedItem(slot).intValue() != 0;

        final UInteger modifierType, modifierValue;

        {
            final ItemType type = ItemType.ofName(name);
            final Optional<SaveFileOffsetHash> modifierTypeHash = modifierTypeHash(type);
            final Optional<SaveFileOffsetHash> modifierValueHash = modifierValueHash(type);
            if (modifierTypeHash.isEmpty() || modifierValueHash.isEmpty()) return;

            modifierType = readWeaponModifierType(modifierTypeHash.get(), modifierIndex);
            modifierValue = readWeaponModifierValue(modifierValueHash.get(), modifierIndex);
        }

        final Modifier modifier = Modifier.of(modifierType, modifierValue);
        final WeaponItem weaponItem = new WeaponItem(name, durability, modifier, equipped);
        items.add(weaponItem);
    }

    @NotNull
    public List<Item> items() {
        return items;
    }

    @NotNull
    public List<WeaponItem> weapons() {
        return items.stream()
                .filter(i -> i instanceof WeaponItem)
                .map(i -> (WeaponItem) i)
                .toList();
    }

    @NotNull
    private Optional<SaveFileOffsetHash> modifierTypeHash(@NotNull final ItemType type) {
        return Optional.ofNullable(switch (type) {
            case MELEE -> SaveFileOffsetHash.WEAPON_MODIFIER_TYPES;
            case BOW -> SaveFileOffsetHash.BOW_MODIFIER_TYPES;
            case SHIELD -> SaveFileOffsetHash.SHIELD_MODIFIER_TYPES;
            default -> null;
        });
    }

    @NotNull
    private Optional<SaveFileOffsetHash> modifierValueHash(@NotNull final ItemType type) {
        return Optional.ofNullable(switch (type) {
            case MELEE -> SaveFileOffsetHash.WEAPON_MODIFIER_VALUES;
            case BOW -> SaveFileOffsetHash.BOW_MODIFIER_VALUES;
            case SHIELD -> SaveFileOffsetHash.SHIELD_MODIFIER_VALUES;
            default -> null;
        });
    }

    @NotNull
    public ParsedSaveFile saveFile() {
        if (saveFile == null) throw new ParsedSaveFileException("Savefile is null");
        return saveFile;
    }

    private int savefileItemOffset(final int slot) {
        return saveFile().offsetByLoadedHash(SaveFileOffsetHash.ITEMS).orElseThrow(ParsedSaveFileException::new) + slot * 128;
    }

    private int savefileItemValueOffset(final int slot) {
        return saveFile().offsetByLoadedHash(SaveFileOffsetHash.ITEMS_VALUE).orElseThrow(ParsedSaveFileException::new) + slot * 8;
    }

    private int savefileWeaponModifierTypeOffset(@NotNull final SaveFileOffsetHash hash, final int slot) {
        return saveFile().offsetByLoadedHash(hash).orElseThrow(ParsedSaveFileException::new) + slot * 8;
    }

    private int savefileWeaponModifierValueOffset(@NotNull final SaveFileOffsetHash hash, final int slot) {
        return saveFile().offsetByLoadedHash(hash).orElseThrow(ParsedSaveFileException::new) + slot * 8;
    }

    private void writeItemName(final int slot, @NotNull final String itemName) {
        saveFile().saveFile().writeString64(savefileItemOffset(slot), itemName);
    }

    @NotNull
    private Optional<String> readItemName(final int slot)  {
        return saveFile().saveFile().readString64(savefileItemOffset(slot));
    }

    private void writeItemValue(final int slot, @NotNull final UInteger value) {
        saveFile().saveFile().writeUint32(savefileItemValueOffset(slot), value);
    }

    @NotNull
    private UInteger readItemValue(final int slot)  {
        return saveFile().saveFile().readUint32(savefileItemValueOffset(slot));
    }

    private int savefileEquippedItemOffset(final int slot) {
        return saveFile().offsetByLoadedHash(SaveFileOffsetHash.ITEMS_EQUIPPED).orElseThrow(ParsedSaveFileException::new) + slot * 8;
    }

    @NotNull
    private UInteger readEquippedItem(final int slot)  {
        return saveFile().saveFile().readUint32(savefileEquippedItemOffset(slot));
    }

    private void writeEquippedItem(final int slot, @NotNull final UInteger equipped) {
        saveFile().saveFile().writeUint32(savefileEquippedItemOffset(slot), equipped);
    }

    private void writeWeaponModifierType(@NotNull final SaveFileOffsetHash hash, final int slot, @NotNull final UInteger type) {
        saveFile().saveFile().writeUint32(savefileWeaponModifierTypeOffset(hash, slot), type);
    }

    @NotNull
    private UInteger readWeaponModifierType(@NotNull final SaveFileOffsetHash hash, final int slot) {
        return saveFile().saveFile().readUint32(savefileWeaponModifierTypeOffset(hash, slot));
    }

    private void writeWeaponModifierValue(@NotNull final SaveFileOffsetHash hash, final int slot, @NotNull final UInteger value) {
        saveFile().saveFile().writeUint32(savefileWeaponModifierValueOffset(hash, slot), value);
    }

    @NotNull
    private UInteger readWeaponModifierValue(@NotNull final SaveFileOffsetHash hash, final int slot) {
        return saveFile().saveFile().readUint32(savefileWeaponModifierValueOffset(hash, slot));
    }

}
