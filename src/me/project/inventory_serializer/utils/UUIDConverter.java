package me.project.inventory_serializer.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDConverter {
    public static byte[] uuidToBytes (UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
    }

    public static UUID uuidFromBytes (byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        return new UUID(mostSigBits, leastSigBits);
    }
}
