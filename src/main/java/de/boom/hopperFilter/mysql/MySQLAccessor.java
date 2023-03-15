package de.boom.hopperFilter.mysql;

import de.boom.hopperFilter.HopperFilter;
import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;
import de.boom.hopperFilter.mysql.dataobjects.FilterItem;
import org.bukkit.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MySQLAccessor {

    public static FilterHopper createHopper(UUID creatorUUID, String uuid) {
        String sql = "INSERT INTO hopper (creatorUUID, uuid,enabled, whitelist) " +
                "VALUES (?, ?, false, true);";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, creatorUUID.toString());
            stmt.setString(2, uuid);

            stmt.execute();
            FilterHopper newFilterhopper = new FilterHopper(creatorUUID, uuid, false, true);
            HopperFilter.getInstance().addIntoFilterHopperMap(newFilterhopper);
            return newFilterhopper;
        } catch (SQLException throwables) {
            if(!(throwables instanceof SQLIntegrityConstraintViolationException)){
                throwables.printStackTrace();
            }
            return null;
        }
    }


    public static HashMap<String, FilterHopper> loadAllFilterHopper(){
        String loadHopperSQL = "SELECT * FROM hopper";
        HashMap<String, FilterHopper> hopperMap = new HashMap<>();
        try (Connection connection = HopperFilter.getDatapool().getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(loadHopperSQL)){
                ResultSet set = stmt.executeQuery();

                while(set.next()){
                    String uuid = set.getString("uuid");
                    UUID creatorUUID = UUID.fromString( set.getString("creatorUUID"));
                    boolean whitelist = set.getBoolean("whitelist");
                    boolean enabled = set.getBoolean("enabled");
                    hopperMap.put(uuid, new FilterHopper(creatorUUID, uuid, enabled, whitelist));
                }


                for(String uuid : hopperMap.keySet()){
                    ArrayList<FilterItem> items = new ArrayList<>();
                    String itemSQL = "SELECT * FROM items WHERE hopper_id = ? ORDER BY id";
                    try (PreparedStatement itemStmt = connection.prepareStatement(itemSQL)){
                        itemStmt.setString(1, uuid);

                        ResultSet itemSet = itemStmt.executeQuery();
                        while(itemSet.next()){
                            items.add(new FilterItem(Material.valueOf(itemSet.getString("material"))));
                        }
                        hopperMap.get(uuid).setItems(items);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

                set.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return hopperMap;
    }

    public static void insertItem(String uuid, Material material) throws SQLException {
        String sql = "INSERT INTO items (hopper_id, material) VALUES (?, ?);";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, material.toString());

            stmt.execute();
        }
    }

    public static void removeItem(String uuid, Material material) throws SQLException {
        String sql = "DELETE FROM items WHERE hopper_id = ? AND material = ?;";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, material.toString());

            stmt.execute();
        }
    }

    public static void toggleHopper(String uuid, boolean enabled) throws SQLException {
        String sql = "UPDATE hopper SET enabled = ? WHERE uuid = ?;";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, enabled);
            stmt.setString(2, uuid);

            stmt.execute();
        }
    }

    public static void toggleHopperWhitelist(String uuid, boolean enabled) throws SQLException{
        String sql = "UPDATE hopper SET whitelist = ? WHERE uuid = ?;;";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, enabled);
            stmt.setString(2, uuid);

            stmt.execute();
        }
    }

    public static void removeHopper(String uuid) throws SQLException {
        String sql = "DELETE FROM hopper WHERE uuid = ?;";
        try (Connection connection = HopperFilter.getDatapool().getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.execute();
        }
    }

}
