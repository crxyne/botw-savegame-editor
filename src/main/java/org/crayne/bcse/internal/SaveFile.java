package org.crayne.bcse.internal;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joou.UInteger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class SaveFile {

    @NotNull
    private final ByteBuffer buffer;

    public SaveFile(@NotNull final File saveFile) {
        try (final FileInputStream fis = new FileInputStream(saveFile)) {
            final byte[] bytes = fis.readAllBytes();
            this.buffer = ByteBuffer.wrap(bytes);
        } catch (final IOException e) {
            throw new SaveFileException(e);
        }
    }

    @NotNull
    public ByteBuffer buffer() {
        return buffer;
    }

    @NotNull
    public UInteger readUint32(final int offset) {
        return UInteger.valueOf(buffer.getInt(offset));
    }

    public void writeUint32(final int offset, @NotNull final UInteger value) {
        buffer.putInt(offset, value.intValue());
    }

    @NotNull
    public Float readFloat32(final int offset) {
        return buffer.getFloat(offset);
    }

    public void writeFloat32(final int offset, final float value) {
        buffer.putFloat(offset, value);
    }

    @NotNull
    private Optional<String> readStringInternal(final int offset, final int length) {
        final StringBuilder result = new StringBuilder();
        if (buffer.get(offset) <= 0) return Optional.empty();
        for (int i = 0; i < length && offset + i < buffer.array().length && buffer.get(offset + i) > 0; i++) {
            result.append(Character.toChars(buffer.get(offset + i)));
        }
        return Optional.of(result.toString());
    }

    @NotNull
    public Optional<String> readString(int offset, final int length) {
        final int finalOffset = offset;
        if (readStringInternal(offset, 4).isEmpty()) return Optional.empty();

        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final String str = readStringInternal(offset, 4).orElse("");
            text.append(str);
            offset += 8;
        }
        return Optional.of(text.toString());
    }

    public void writeString(int offset, @NotNull final String str, final int length) {
        if (length == 0 || str.isEmpty()) return;
        final int initialOffset = offset;

        for (int i = 0; i < length && i < str.length(); i++) {
            buffer.put(offset, new byte[] {0, 0, 0, 0});
            final String substr4Bytes = StringUtils.substring(str, i * 4, i * 4 + 4);

            for (int j = 0; j < substr4Bytes.length(); j++) {
                buffer.put(offset + j, (byte) substr4Bytes.charAt(j));
            }
            offset += 8;
        }
    }

    @NotNull
    public Optional<String> readString64(final int offset, final int arrayIndex) {
        return readString(offset + 0x80 * arrayIndex, 16);
    }

    @NotNull
    public Optional<String> readString64(final int offset) {
        return readString64(offset, 0);
    }

    public void writeString64(final int offset, @NotNull final String str, final int arrayIndex) {
        writeString(offset + 0x80 * arrayIndex, str, 16);
    }

    public void writeString64(final int offset, @NotNull final String str) {
        writeString64(offset, str, 0);
    }

    @NotNull
    public Optional<String> readString256(final int offset) {
        return readString(offset, 64);
    }

    public void writeString256(final int offset, @NotNull final String str) {
        writeString(offset, str, 64);
    }

}
