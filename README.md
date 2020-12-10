# InventorySerializer

*InventorySerializer* позволяет сериализововать, десериализововать и хранить Ваши инвентари *Bukkit* в базе данных *Sqlite3*.
***
Пример использования:

```java
SerializedInventoryDataBase dataBase =
                new SerializedInventoryDataBase(super.getDataFolder().getAbsolutePath() + File.separator + "inventories.sqlite3");

UUID invId = dataBase.addInventory(inventory, inventoryTitle);
if (invId == null) return;

inventory.setItem(5, new ItemStack(Material.DIAMOND_HOE));
dataBase.updateInventory(invId, inventory, inventoryTitle);

Inventory parsedInventory = dataBase.getInventory(invId);
if (parsedInventory == null) return;

// Prints: The content of the inventory is the same
if (Arrays.equals(inventory.getContents(), parsedInventory.getContents()))
    System.out.println("The content of the inventory is the same");
else System.out.println("The content of the inventory isn't the same");

dataBase.removeInventory(invId);
dataBase.close();
```
***
Вы можете копировать и использовать код, представленный в репозитории без указания ссылки на источник.
