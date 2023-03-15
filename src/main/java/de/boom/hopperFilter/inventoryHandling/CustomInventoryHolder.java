package de.boom.hopperFilter.inventoryHandling;

import de.boom.hopperFilter.HopperFilter;
import de.boom.hopperFilter.configuration.StringTable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {
    private final String uuid;
    private final Inventory inv;

    private boolean loading;

    public CustomInventoryHolder(String uuid) {
        this.uuid = uuid;
        loading = false;
        this.inv = HopperFilter.getInstance().getServer().createInventory(this, 45, StringTable.INVENTORYNAME_CONFIG_MENU.getComponentBuilder());
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getUUID() {
        return uuid;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
