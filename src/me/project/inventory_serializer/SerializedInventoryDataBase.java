package me.project.inventory_serializer;

import me.project.inventory_serializer.utils.InventorySerializer;
import me.project.inventory_serializer.utils.UUIDConverter;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SerializedInventoryDataBase implements AutoCloseable {
    private final Connection connection;
    private final Statement statement;
    private final PreparedStatement insertData;
    private final PreparedStatement getInventory;
    private final PreparedStatement updateDataById;
    private final PreparedStatement removeDataById;

    public SerializedInventoryDataBase(String dbFileName) throws SQLException {
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

    @Nullable
    public UUID addInventory(Inventory inventory, String title) {
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

    public UUID addInventory(Inventory inventory) {
        return addInventory(inventory, inventory.getType().getDefaultTitle());
    }

    public boolean removeInventory(UUID invId) {
        try {
            removeDataById.setBytes(1, UUIDConverter.uuidToBytes(invId));
            return removeDataById.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return false;
    }

    public Inventory getInventory(UUID invId) {
        try {
            getInventory.setBytes(1, UUIDConverter.uuidToBytes(invId));
            ResultSet result = getInventory.executeQuery();
            if(result.isClosed()) return  null;
            else return InventorySerializer.deserialize(result.getBytes(1));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Cannot deserialize inventory");
        }

        return null;
    }

    public boolean updateInventory(UUID invId, Inventory newInventory, String title) {
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

    public boolean updateInventory(UUID invId, Inventory newInventory) {
        return updateInventory(invId, newInventory, newInventory.getType().getDefaultTitle());
    }

}
