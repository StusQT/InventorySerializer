package me.project.inventory_serializer;

import me.project.inventory_serializer.utils.InventorySerializer;
import me.project.inventory_serializer.utils.UUIDConverter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.WillClose;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

@NotThreadSafe
@WillClose
public class SerializedInventoryDataBase implements AutoCloseable {
    private final Connection connection;
    private final Statement statement;
    private final PreparedStatement insertData;
    private final PreparedStatement getInventory;
    private final PreparedStatement updateDataById;
    private final PreparedStatement removeDataById;

    /**
     * @param dbFileName Name of the database file
     *                   Warning: this class is NOT thread safe.
     *                   Please take care of synchronisation if you are establishing multiple connections to the same database
     * @throws SQLException if a database access error occurs
     */
    public SerializedInventoryDataBase(@NotNull String dbFileName) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
        statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS Inventories (id BLOB(16) PRIMARY KEY, inventoryData BLOB);");

        insertData = connection.prepareStatement("INSERT INTO Inventories (id, inventoryData) VALUES (?, ?);");
        getInventory = connection.prepareStatement("SELECT inventoryData FROM Inventories WHERE id = (?)");
        updateDataById = connection.prepareStatement("UPDATE Inventories SET inventoryData = (?) WHERE id = (?);");
        removeDataById = connection.prepareStatement("DELETE FROM Inventories WHERE id = (?);");
    }

    @Override
    public void close() throws SQLException {
        insertData.close();
        getInventory.close();
        updateDataById.close();
        removeDataById.close();
        statement.close();
        connection.close();
    }

    /**
     * Saves inventory in database
     *
     * @param inventory The inventory to be saved
     * @param title     custom title of inventory
     * @return UUID of inventory in database or null if an exception was thrown.
     * You can use UUID to change or retrieve inventory.
     * @see Bukkit#createInventory(InventoryHolder, int, String)
     **/
    @Nullable
    public UUID addInventory(@NotNull Inventory inventory, @NotNull String title) {
        try {
            UUID invId = UUID.randomUUID();
            insertData.setBytes(1, UUIDConverter.uuidToBytes(invId));
            insertData.setBytes(2, InventorySerializer.serialize(inventory, title));
            insertData.execute();

            return invId;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Cannot serialize inventory");
        }

        return null;
    }

    /**
     * Overloaded version of {@link #addInventory(Inventory, String)}
     * This method uses standard inventory titles
     *
     * @param inventory The inventory to be saved
     * @return UUID of inventory in database or null if an exception was thrown.
     * You can use UUID to change or retrieve inventory.
     */
    public UUID addInventory(@NotNull Inventory inventory) {
        return addInventory(inventory, inventory.getType().getDefaultTitle());
    }

    /**
     * Removes inventory from database
     *
     * @param invId UUID if inventory in database. You can get it from {@link #addInventory(Inventory, String)}
     *              or {@link #addInventory(Inventory)} method
     * @return {@code true} if the deletion was successful, else - {@code false}
     */
    public boolean removeInventory(@NotNull UUID invId) {
        try {
            removeDataById.setBytes(1, UUIDConverter.uuidToBytes(invId));
            return removeDataById.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves inventory from the database
     *
     * @param invId UUID if inventory in database. You can get it from {@link #addInventory(Inventory, String)}
     *              or {@link #addInventory(Inventory)} method
     * @return the received inventory. If an exception was thrown or the inventory does not exist - returns {@code null}
     */
    public Inventory getInventory(@NotNull UUID invId) {
        try {
            getInventory.setBytes(1, UUIDConverter.uuidToBytes(invId));
            ResultSet result = getInventory.executeQuery();
            if (result.isClosed()) return null;
            else {
              Inventory parsedInventory = InventorySerializer.deserialize(result.getBytes(1));
              result.close();
              return parsedInventory;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Cannot deserialize inventory");
        }

        return null;
    }

    /**
     * Updates existing inventory in the database
     *
     * @param invId        UUID if inventory in database. You can get it from {@link #addInventory(Inventory, String)}
     *                     or {@link #addInventory(Inventory)} method
     * @param newInventory new inventory to replace the old one
     * @param title        custom title of inventory
     * @return {@code true} if the update was successful.
     * If an exception was thrown or the inventory that should be updated does not exist - returns {@code false}
     */
    public boolean updateInventory(@NotNull UUID invId, @NotNull Inventory newInventory, @NotNull String title) {
        try {
            updateDataById.setBytes(1, InventorySerializer.serialize(newInventory, title));
            updateDataById.setBytes(2, UUIDConverter.uuidToBytes(invId));

            return updateDataById.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Cannot serialize inventory");
        }

        return false;
    }

    /**
     * Overloaded version of {@link #updateInventory(UUID, Inventory, String) method}
     * This method uses standard inventory titles
     *
     * @param invId        UUID if inventory in database. You can get it from {@link #addInventory(Inventory, String)}
     *                     or {@link #addInventory(Inventory)} method
     * @param newInventory new inventory to replace the old one
     * @return {@code true} if the update was successful.
     * If an exception was thrown or the inventory that should be updated does not exist - returns {@code false}
     */
    public boolean updateInventory(@NotNull UUID invId, @NotNull Inventory newInventory) {
        return updateInventory(invId, newInventory, newInventory.getType().getDefaultTitle());
    }

}
