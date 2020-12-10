package me.project.inventory_serializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        String inventoryTitle = "Super inventory";
        Inventory inventory = Bukkit.createInventory(null, 36, inventoryTitle);
        inventory.setItem(3, new ItemStack(Material.PUMPKIN, 48));

        try {
            testDataBase(inventory, inventoryTitle);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void testDataBase(Inventory inventory, String inventoryTitle) throws SQLException {
        SerializedInventoryDataBase dataBase =
                new SerializedInventoryDataBase(super.getDataFolder().getAbsolutePath() + File.separator + "inventories.sqlite3");

        UUID invId = dataBase.addInventory(inventory, inventoryTitle);
        if (invId == null) return;

        inventory.setItem(5, new ItemStack(Material.DIAMOND_HOE));
        dataBase.updateInventory(invId, inventory, inventoryTitle);

        Inventory parsedInventory = dataBase.getInventory(invId);
        if (parsedInventory == null) return;

        if (Arrays.equals(inventory.getContents(), parsedInventory.getContents()))
            System.out.println("The content of the inventory is the same");
        else System.out.println("The content of the inventory isn't the same");

        // Prints: The content of the inventory is the same
        dataBase.removeInventory(invId);
        dataBase.close();
    }
}
