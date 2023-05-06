package org.crayne.bcse.parsed;

import org.crayne.bcse.game.inventory.Inventory;
import org.crayne.bcse.game.misc.Rune;
import org.crayne.bcse.internal.SaveFile;
import org.crayne.bcse.internal.SaveFileOffsetHash;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joou.UInteger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ParsedSaveFile {

    @NotNull
    private final SaveFile saveFile;

    private UInteger rupees;
    private UInteger mons;
    private UInteger maxHearts;
    private float maxStamina;

    private UInteger relicGerudoAmount;
    private UInteger relicGoronAmount;
    private UInteger relicRitoAmount;

    private UInteger korokSeedAmount;
    private UInteger defeatedHinox;
    private UInteger defeatedTalus;
    private UInteger defeatedMolduga;
    private UInteger timePlayed;
    private boolean hasMotorcycle;

    private float positionX;
    private float positionY;
    private float positionZ;

    @Nullable
    private String mapName;

    @Nullable
    private String mapType;

    @Nullable
    private Inventory inventory;

    private float horsePositionX;
    private float horsePositionY;
    private float horsePositionZ;

    private UInteger selectedRune;

    private final Map<SaveFileOffsetHash, Integer> offsetHashMap = new HashMap<>();

    public ParsedSaveFile(@NotNull final File file) {
        this.saveFile = new SaveFile(file);
    }

    public ParsedSaveFile(@NotNull final SaveFile file) {
        this.saveFile = file;
    }

    @NotNull
    public SaveFile saveFile() {
        return saveFile;
    }

    public void load() {
        offsetHashMap.clear();
        offsetHashMap.putAll(loadOffsets());

        if (Arrays.stream(SaveFileOffsetHash.values()).anyMatch(h -> h.mustExist() && !offsetHashMap.containsKey(h)))
            throw new ParsedSaveFileException("Corrupted savefile was found, unable to load all offsets from hashes");

        rupees              = readUint32(SaveFileOffsetHash.RUPEES);
        mons                = readUint32(SaveFileOffsetHash.MONS);
        maxHearts           = readUint32(SaveFileOffsetHash.MAX_HEARTS);
        relicGerudoAmount   = readUint32(SaveFileOffsetHash.RELIC_GERUDO_AMOUNT);
        relicGoronAmount    = readUint32(SaveFileOffsetHash.RELIC_GORON_AMOUNT);
        relicRitoAmount     = readUint32(SaveFileOffsetHash.RELIC_RITO_AMOUNT);
        korokSeedAmount     = readUint32(SaveFileOffsetHash.KOROK_SEED_AMOUNT);
        defeatedHinox       = readUint32(SaveFileOffsetHash.DEFEATED_HINOX_AMOUNT);
        defeatedTalus       = readUint32(SaveFileOffsetHash.DEFEATED_TALUS_AMOUNT);
        defeatedMolduga     = readUint32(SaveFileOffsetHash.DEFEATED_MOLDUGA_AMOUNT);
        timePlayed          = readUint32(SaveFileOffsetHash.TIME_PLAYED);
        hasMotorcycle       = readUint32(SaveFileOffsetHash.HAS_MOTORCYCLE).intValue() != 0;

        maxStamina          = readFloat32(SaveFileOffsetHash.MAX_STAMINA) / 1000.0f;

        mapName             = readString256(SaveFileOffsetHash.MAP_NAME).orElse(null);
        mapType             = readString256(SaveFileOffsetHash.MAP_TYPE).orElse(null);

        positionX           = readFloat32(SaveFileOffsetHash.PLAYER_POSITION);
        positionY           = readFloat32(SaveFileOffsetHash.PLAYER_POSITION, 8);
        positionZ           = readFloat32(SaveFileOffsetHash.PLAYER_POSITION, 16);

        horsePositionX      = readFloat32(SaveFileOffsetHash.HORSE_POSITION);
        horsePositionY      = readFloat32(SaveFileOffsetHash.HORSE_POSITION, 8);
        horsePositionZ      = readFloat32(SaveFileOffsetHash.HORSE_POSITION, 16);

        selectedRune        = readUint32(SaveFileOffsetHash.SELECTED_RUNE);

        inventory           = new Inventory(this);
        inventory.loadFromSavefile();
    }

    public void saveToFile(@NotNull final File file) {
        save();
        writeToFile(file);
    }

    public void writeToFile(@NotNull final File file) {
        try (final FileOutputStream fos = new FileOutputStream(file)) {
            final FileChannel channel = fos.getChannel();
            channel.write(saveFile.buffer());
            channel.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        if (mapName == null || mapType == null) throw new ParsedSaveFileException("Cannot save; map name = " + mapName + ", map type = " + mapType);
        if (inventory == null) throw new ParsedSaveFileException("Cannot save; Inventory is null");

        writeUint32(SaveFileOffsetHash.RUPEES, rupees);
        writeUint32(SaveFileOffsetHash.MONS, mons);
        writeUint32(SaveFileOffsetHash.MAX_HEARTS, maxHearts);
        writeUint32(SaveFileOffsetHash.RELIC_GERUDO_AMOUNT, relicGerudoAmount);
        writeUint32(SaveFileOffsetHash.RELIC_GORON_AMOUNT, relicGoronAmount);
        writeUint32(SaveFileOffsetHash.RELIC_RITO_AMOUNT, relicRitoAmount);
        writeUint32(SaveFileOffsetHash.KOROK_SEED_AMOUNT, korokSeedAmount);
        writeUint32(SaveFileOffsetHash.DEFEATED_HINOX_AMOUNT, defeatedHinox);
        writeUint32(SaveFileOffsetHash.DEFEATED_TALUS_AMOUNT, defeatedTalus);
        writeUint32(SaveFileOffsetHash.DEFEATED_MOLDUGA_AMOUNT, defeatedMolduga);
        writeUint32(SaveFileOffsetHash.TIME_PLAYED, timePlayed);

        if (offsetHashMap.containsKey(SaveFileOffsetHash.HAS_MOTORCYCLE))
            writeUint32(SaveFileOffsetHash.HAS_MOTORCYCLE, UInteger.valueOf(hasMotorcycle ? 1 : 0));

        writeFloat32(SaveFileOffsetHash.MAX_STAMINA, maxStamina * 1000.0f);

        writeString256(SaveFileOffsetHash.MAP_NAME, mapName);
        writeString256(SaveFileOffsetHash.MAP_TYPE, mapType);

        writeFloat32(SaveFileOffsetHash.PLAYER_POSITION, positionX);
        writeFloat32(SaveFileOffsetHash.PLAYER_POSITION, 8, positionY);
        writeFloat32(SaveFileOffsetHash.PLAYER_POSITION, 16, positionZ);

        writeFloat32(SaveFileOffsetHash.HORSE_POSITION, horsePositionX);
        writeFloat32(SaveFileOffsetHash.HORSE_POSITION, 8, horsePositionY);
        writeFloat32(SaveFileOffsetHash.HORSE_POSITION, 16, horsePositionZ);

        writeUint32(SaveFileOffsetHash.SELECTED_RUNE, selectedRune);

        inventory.saveToSavefile();
    }

    @NotNull
    public Optional<Rune> selectedRuneAsRune() {
        return selectedRune == null ? Optional.empty() : Rune.ofId(selectedRune.intValue());
    }

    public UInteger selectedRune() {
        return selectedRune;
    }

    public void selectedRune(final UInteger selectedRune) {
        this.selectedRune = selectedRune;
    }

    public void selectedRune(@NotNull final Rune rune) {
        this.selectedRune = UInteger.valueOf(rune.id());
    }

    @NotNull
    public Optional<Inventory> inventory() {
        return Optional.ofNullable(inventory);
    }

    public void defeatedHinox(final UInteger defeatedHinox) {
        this.defeatedHinox = defeatedHinox;
    }

    public void defeatedMolduga(final UInteger defeatedMolduga) {
        this.defeatedMolduga = defeatedMolduga;
    }

    public void defeatedTalus(final UInteger defeatedTalus) {
        this.defeatedTalus = defeatedTalus;
    }

    public void hasMotorcycle(final boolean hasMotorcycle) {
        this.hasMotorcycle = hasMotorcycle;
    }

    public void horsePositionX(final float horsePositionX) {
        this.horsePositionX = horsePositionX;
    }

    public void horsePositionY(final float horsePositionY) {
        this.horsePositionY = horsePositionY;
    }

    public void horsePositionZ(final float horsePositionZ) {
        this.horsePositionZ = horsePositionZ;
    }

    public void inventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    public void korokSeedAmount(final UInteger korokSeedAmount) {
        this.korokSeedAmount = korokSeedAmount;
    }

    public void mapName(final String mapName) {
        this.mapName = mapName;
    }

    public void mapType(final String mapType) {
        this.mapType = mapType;
    }

    public void maxHearts(final UInteger maxHearts) {
        this.maxHearts = maxHearts;
    }

    public void mons(final UInteger mons) {
        this.mons = mons;
    }

    public void maxStamina(final float maxStamina) {
        this.maxStamina = maxStamina;
    }

    public void positionX(final float positionX) {
        this.positionX = positionX;
    }

    public void positionY(final float positionY) {
        this.positionY = positionY;
    }

    public void positionZ(final float positionZ) {
        this.positionZ = positionZ;
    }

    public void relicGerudoAmount(final UInteger relicGerudoAmount) {
        this.relicGerudoAmount = relicGerudoAmount;
    }

    public void relicGoronAmount(final UInteger relicGoronAmount) {
        this.relicGoronAmount = relicGoronAmount;
    }

    public void rupees(final UInteger rupees) {
        this.rupees = rupees;
    }

    public void relicRitoAmount(final UInteger relicRitoAmount) {
        this.relicRitoAmount = relicRitoAmount;
    }

    public void timePlayed(final UInteger timePlayed) {
        this.timePlayed = timePlayed;
    }

    public void writeUint32(@NotNull final SaveFileOffsetHash hash, final int additionalOffset, @NotNull final UInteger value) {
        offsetByLoadedHash(hash).stream()
                .peek(i -> saveFile.writeUint32(i + additionalOffset, value))
                .findAny()
                .orElseThrow(ParsedSaveFileException::new);
    }

    public void writeUint32(@NotNull final SaveFileOffsetHash hash, @NotNull final UInteger value) {
        writeUint32(hash, 0, value);
    }

    @NotNull
    public UInteger readUint32(@NotNull final SaveFileOffsetHash hash, final int additionalOffset) {
        return offsetByLoadedHash(hash)
                .map(i -> saveFile.readUint32(i + additionalOffset))
                .orElse(UInteger.valueOf(0));
    }

    @NotNull
    public UInteger readUint32(@NotNull final SaveFileOffsetHash hash) {
        return readUint32(hash, 0);
    }

    public void writeFloat32(@NotNull final SaveFileOffsetHash hash, final int additionalOffset, final float value) {
        offsetByLoadedHash(hash).stream()
                .peek(i -> saveFile.writeFloat32(i + additionalOffset, value))
                .findAny()
                .orElseThrow(ParsedSaveFileException::new);
    }

    public void writeFloat32(@NotNull final SaveFileOffsetHash hash, final float value) {
        writeFloat32(hash, 0, value);
    }

    public float readFloat32(@NotNull final SaveFileOffsetHash hash, final int additionalOffset) {
        return offsetByLoadedHash(hash)
                .map(i -> saveFile.readFloat32(i + additionalOffset))
                .orElse(0.0f);
    }

    public float readFloat32(@NotNull final SaveFileOffsetHash hash) {
        return readFloat32(hash, 0);
    }

    public void writeString256(@NotNull final SaveFileOffsetHash hash, final int additionalOffset, @NotNull final String str) {
        offsetByLoadedHash(hash).stream()
                .peek(i -> saveFile.writeString256(i + additionalOffset, str))
                .findAny()
                .orElseThrow(ParsedSaveFileException::new);
    }

    public void writeString256(@NotNull final SaveFileOffsetHash hash, @NotNull final String str) {
        writeString256(hash, 0, str);
    }

    @NotNull
    public Optional<String> readString256(@NotNull final SaveFileOffsetHash hash, final int additionalOffset) {
        return offsetByLoadedHash(hash).flatMap(i -> saveFile.readString256(i + additionalOffset));
    }

    @NotNull
    public Optional<String> readString256(@NotNull final SaveFileOffsetHash hash) {
        return readString256(hash, 0);
    }

    public void writeString64(@NotNull final SaveFileOffsetHash hash, final int additionalOffset, @NotNull final String str) {
        offsetByLoadedHash(hash).stream()
                .peek(i -> saveFile.writeString64(i + additionalOffset, str))
                .findAny()
                .orElseThrow(ParsedSaveFileException::new);
    }

    public void writeString64(@NotNull final SaveFileOffsetHash hash, @NotNull final String str) {
        writeString64(hash, 0, str);
    }

    @NotNull
    public Optional<String> readString64(@NotNull final SaveFileOffsetHash hash, final int additionalOffset) {
        return offsetByLoadedHash(hash).flatMap(i -> saveFile.readString64(i + additionalOffset));
    }


    @NotNull
    public Optional<String> readString64(@NotNull final SaveFileOffsetHash hash) {
        return readString64(hash, 0);
    }

    @NotNull
    public Optional<Integer> offsetByLoadedHash(@NotNull final SaveFileOffsetHash hash) {
        return Optional.ofNullable(offsetHashMap.get(hash));
    }

    @NotNull
    private Optional<Integer> findOffsetByHash(@NotNull final SaveFileOffsetHash hash) {
        for (int i = 0x04; i < saveFile.buffer().array().length; i += 8)
            if (hash.offsetHash().equals(saveFile.readUint32(i))) return Optional.of(i + 4);

        return Optional.empty();
    }

    @NotNull
    private Map<SaveFileOffsetHash, Integer> loadOffsets() {
        final Map<SaveFileOffsetHash, Integer> offsets = new HashMap<>();
        for (@NotNull final SaveFileOffsetHash hash : SaveFileOffsetHash.values()) {
            final Optional<Integer> maybeFoundOffset = findOffsetByHash(hash);
            maybeFoundOffset.ifPresent(i -> offsets.put(hash, i));
        }
        return offsets;
    }

    public boolean hasMotorcycle() {
        return hasMotorcycle;
    }

    public float horsePositionX() {
        return horsePositionX;
    }

    public float horsePositionY() {
        return horsePositionY;
    }

    public float horsePositionZ() {
        return horsePositionZ;
    }

    public float positionX() {
        return positionX;
    }

    public float positionY() {
        return positionY;
    }

    public float positionZ() {
        return positionZ;
    }

    @NotNull
    public Optional<String> mapName() {
        return Optional.ofNullable(mapName);
    }

    @NotNull
    public Optional<String> mapType() {
        return Optional.ofNullable(mapType);
    }

    public UInteger defeatedHinox() {
        return defeatedHinox;
    }

    public UInteger defeatedMolduga() {
        return defeatedMolduga;
    }

    public UInteger defeatedTalus() {
        return defeatedTalus;
    }

    public UInteger korokSeedAmount() {
        return korokSeedAmount;
    }

    public UInteger maxHearts() {
        return maxHearts;
    }

    public float maxStamina() {
        return maxStamina;
    }

    public UInteger mons() {
        return mons;
    }

    public UInteger relicGerudoAmount() {
        return relicGerudoAmount;
    }

    public UInteger relicGoronAmount() {
        return relicGoronAmount;
    }

    public UInteger relicRitoAmount() {
        return relicRitoAmount;
    }

    public UInteger rupees() {
        return rupees;
    }

    public UInteger timePlayed() {
        return timePlayed;
    }

    @NotNull
    public String toString() {
        return "ParsedSaveFile {" +
                "\n\trupees=" + rupees +
                ", \n\tmons=" + mons +
                ", \n\tmaxHearts=" + maxHearts +
                ", \n\tmaxStamina=" + maxStamina +
                ", \n\trelicGerudoAmount=" + relicGerudoAmount +
                ", \n\trelicGoronAmount=" + relicGoronAmount +
                ", \n\trelicRitoAmount=" + relicRitoAmount +
                ", \n\tkorokSeedCount=" + korokSeedAmount +
                ", \n\tdefeatedHinox=" + defeatedHinox +
                ", \n\tdefeatedTalus=" + defeatedTalus +
                ", \n\tdefeatedMolduga=" + defeatedMolduga +
                ", \n\ttimePlayed=" + timePlayed +
                ", \n\thasMotorcycle=" + hasMotorcycle +
                ", \n\tpositionX=" + positionX +
                ", \n\tpositionY=" + positionY +
                ", \n\tpositionZ=" + positionZ +
                ", \n\tmapName='" + mapName + '\'' +
                ", \n\tmapType='" + mapType + '\'' +
                ", \n\thorsePositionX=" + horsePositionX +
                ", \n\thorsePositionY=" + horsePositionY +
                ", \n\thorsePositionZ=" + horsePositionZ +
                "\n}";
    }

}
