package me.project.inventory_serializer.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class InventorySerializer {
    /**
     * Serializes the inventory into bytes.
     * The structure of bytes in the serialized inventory:
     * 16 bytes - UUID of inventory holder. Filled with zeros if the holder is {@code null},
     * 1 byte - title length (max title length is 128 symbols),
     * then inventory title,
     * 4 bytes - inventory size,
     * the remaining bytes are content from the inventory.
     *
     * @param inventory inventory, that will be serialized
     * @param title     custom title of the inventory
     * @return the byte representation of the inventory
     * @throws IOException if IO error occurs
     * @see #deserialize(byte[])
     * @see BukkitObjectOutputStream
     */
    public static byte[] serialize(Inventory inventory, String title) throws IOException {
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream)) {
                InventoryHolder holder = inventory.getHolder();
                if (holder == null) {
                    objectOutputStream.writeLong(0L);
                    objectOutputStream.writeLong(0L);
                } else {
                    UUID uuid = ((OfflinePlayer) holder).getUniqueId();
                    objectOutputStream.writeLong(uuid.getMostSignificantBits());
                    objectOutputStream.writeLong(uuid.getLeastSignificantBits());
                }

                objectOutputStream.writeByte(title.length());
                objectOutputStream.writeBytes(title);

                objectOutputStream.writeInt(inventory.getSize());
                for (ItemStack is : inventory.getContents())
                    objectOutputStream.writeObject(is);

                return arrayOutputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Deserializes the inventory from bytes.
     * Note that if the owner of the inventory is not online during deserialization, inventory will be shared.
     * @param serializedData the byte representation of the inventory
     * @return deserialized inventory
     * @throws IOException if IO error occurs or content cannot be parsed.
     * @see #serialize(Inventory, String)
     * @see BukkitObjectInputStream
     */
    public static Inventory deserialize(byte[] serializedData) throws IOException {
        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(serializedData)) {
            try (BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(byteInputStream)) {
                InventoryHolder holder;
                long mostSigBits = objectInputStream.readLong();
                long leastSigBits = objectInputStream.readLong();
                if (mostSigBits == 0L && leastSigBits == 0L) holder = null;
                else holder = Bukkit.getOfflinePlayer(new UUID(mostSigBits, leastSigBits)).getPlayer();

                byte titleLength = objectInputStream.readByte();
                byte[] titleBytes = new byte[titleLength];
                objectInputStream.readFully(titleBytes);
                String title = new String(titleBytes);

                int inventorySize = objectInputStream.readInt();
                Inventory deserializedInventory = Bukkit.createInventory(holder, inventorySize, title);
                for (int i = 0; i < inventorySize; ++i)
                    deserializedInventory.setItem(i, (ItemStack) objectInputStream.readObject());

                return deserializedInventory;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
