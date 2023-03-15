package de.boom.hopperFilter;

import de.boom.hopperFilter.callbacks.LoadAllHopperCallback;
import de.boom.hopperFilter.configuration.DefaultConfig;
import de.boom.hopperFilter.configuration.StringTable;
import de.boom.hopperFilter.inventoryHandling.ConfigurationInventory;
import de.boom.hopperFilter.listener.HopperListener;
import de.boom.hopperFilter.mysql.MySQLAccessor;
import de.boom.hopperFilter.mysql.MySQLDataPool;
import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;

/*
    TODO
     Not discussed Problems:
      1. What if user trys to edit a Hopper which was previously edited by another user
      2. If a user with permission for more than 3 Slots loses this permission, what should happen to the hopper
      -
      Proposition to solve these issues. Give hopper an owner, slot count is determined by his permissions. If user loses permission for slot 4 and/or 5 database keeps those but the filter is ignored
      -
      Concerns:
       1. Can be very resource intense as the InventoryMoveItemEvent is triggered very often
       2. Impractical or very complex - If the hopper encounters an item that should not be moved it stalls, adding to the problem above. If we would like to prevent this, logic for moving the item
       within the source inventory would be needed, but due to the way the InventoryMoveItemEvent is implemented it would be very complex
       -
       Assumptions:
        Plugin is present which stores player permission even if offline - as a placeholder for that vault is used
    TODO
      2. Make plugin verbose
 */

public class HopperFilter extends JavaPlugin {

    private static Permission perms = null;
    private static HopperFilter instance;
    public static MySQLDataPool datapool;
    private HashMap<String, FilterHopper> filterHopperMap;

    public void onEnable(){
        instance = this;
        DefaultConfig.load(this);
        StringTable.load(this);
        try {
            datapool = new MySQLDataPool(DefaultConfig.DATABASE_HOST.getString(), DefaultConfig.DATABASE_PORT.getInt(), DefaultConfig.DATABASE_DATABASENAME.getString(), DefaultConfig.DATABASE_USERNAME.getString(), DefaultConfig.DATABASE_PASSWORD.getString());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        registerListeners();
        setupPermissions();
        loadHopper(result -> {
            filterHopperMap = result;
            for (String uuid : result.keySet()) {
                FilterHopper hopper = result.get(uuid);
                Location loc = FilterHopper.createLocationFromUUID(uuid);
                if(!loc.getBlock().getType().equals(Material.HOPPER)){
                    try {
                        hopper.delete();
                    } catch (SQLException ignore) {}
                }
            }
        });
    }

    public void onDisable(){
        instance = null;
        try {
            datapool.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        datapool = null;
    }


    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    private void loadHopper(final LoadAllHopperCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final HashMap<String, FilterHopper> result = MySQLAccessor.loadAllFilterHopper();
            Bukkit.getScheduler().runTask(HopperFilter.getInstance(), () -> callback.onQueryDone(result));
        });
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new HopperListener(), this);
        Bukkit.getPluginManager().registerEvents(new ConfigurationInventory(), this);
    }

    public static HopperFilter getInstance() {
        return instance;
    }

    public static MySQLDataPool getDatapool() {
        return datapool;
    }

    public HashMap<String, FilterHopper> getFilterHopperMap() {
        return filterHopperMap;
    }

    public void addIntoFilterHopperMap(FilterHopper hopper){
        this.filterHopperMap.put(hopper.getUuid(), hopper);
    }

    public static Permission getPerms() {
        return perms;
    }
}
