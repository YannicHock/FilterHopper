package de.boom.hopperFilter.mysql.dataobjects;

import de.boom.hopperFilter.HopperFilter;
import de.boom.hopperFilter.mysql.MySQLAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class FilterHopper {

    private final String uuid;
    private boolean whitelist;
    private boolean enabled;
    private ArrayList<FilterItem> items;

    private final UUID creatorUUID;

    public FilterHopper(UUID creatorUUID, String uuid, boolean enabled, boolean whitelist){
        this.uuid = uuid;
        this.enabled = enabled;
        this.whitelist = whitelist;
        this.creatorUUID = creatorUUID;
        this.items = new ArrayList<>();
    }

    public static String createUUIDFromLocation(Location loc){
        return loc.getWorld().getName()
                + "_" + loc.getBlockX()
                + "_" + loc.getBlockY()
                + "_" + loc.getBlockZ();
    }

    public static Location createLocationFromUUID(String uuid){
        String[] deserialized = uuid.split("_");
        return new Location(Bukkit.getWorld(deserialized[0]), Integer.parseInt(deserialized[1]), Integer.parseInt(deserialized[2]), Integer.parseInt(deserialized[3]));
    }

    public ArrayList<FilterItem> getItems() {
        return items;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setItems(ArrayList<FilterItem> items) {
        this.items = items;
    }


    public void addItem(Material material) throws SQLException {
        if(!containsMaterial(material)){
            MySQLAccessor.insertItem(uuid, material);
            items.add(new FilterItem(material));
        }
    }

    public boolean containsMaterial(Material material){
        for (FilterItem item : items) {
            if(item.getMaterial().equals(material))
                return true;
        }
        return false;
    }

    public int getSlotOfMaterial(Material material){
        return items.indexOf(material);
    }

    public void removeItem(int index) throws SQLException {
        MySQLAccessor.removeItem(uuid, items.get(index).getMaterial());
        items.remove(index);
    }

    public void setEnabled(boolean enabled) throws SQLException {
        MySQLAccessor.toggleHopper(uuid, enabled);
        this.enabled = enabled;
    }
    public void setWhitelist(boolean whitelist) throws SQLException {
        MySQLAccessor.toggleHopperWhitelist(uuid, whitelist);
        this.whitelist = whitelist;
    }

    public void delete() throws SQLException {
        MySQLAccessor.removeHopper(uuid);
        HopperFilter.getInstance().getFilterHopperMap().remove(uuid);
    }

    public UUID getCreatorUUID() {
        return creatorUUID;
    }
}
