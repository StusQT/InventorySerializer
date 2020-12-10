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
