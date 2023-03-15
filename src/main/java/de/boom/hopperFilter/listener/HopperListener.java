package de.boom.hopperFilter.listener;

import de.boom.hopperFilter.HopperFilter;
import de.boom.hopperFilter.configuration.Permissions;
import de.boom.hopperFilter.inventoryHandling.ConfigurationInventory;
import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;
import de.boom.hopperFilter.mysql.dataobjects.FilterItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;
import java.util.ArrayList;

public class HopperListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction().isRightClick() && player.isSneaking() && event.getClickedBlock().getType().equals(Material.HOPPER)){
            event.setCancelled(true);
            new ConfigurationInventory().openInventory(player, event.getClickedBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType().equals(Material.HOPPER)){
            String uuid = FilterHopper.createUUIDFromLocation(event.getBlock().getLocation());
            if(HopperFilter.getInstance().getFilterHopperMap().containsKey(uuid)){
                try {
                    HopperFilter.getInstance().getFilterHopperMap().get(uuid).delete();
                } catch (SQLException e) {
                    e.printStackTrace();
                    event.setCancelled(true);
                }
            }
        }
    }


    private boolean hasPermissionToBeFiltered(FilterHopper hopper, int slot){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(hopper.getCreatorUUID());
        Permissions perm = null;
        switch (slot) {
            case 3 -> perm = Permissions.HOPPER_SLOT_4;
            case 4 -> perm = Permissions.HOPPER_SLOT_5;
            default -> {
            }
        }
        if(perm == null){
            return true;
        }
        return HopperFilter.getPerms().playerHas(Bukkit.getServer().getWorlds().get(0).getName(), offlinePlayer, perm.getNode());
    }

    private ArrayList<Material> getPermissionbasedBlackList(FilterHopper hopper){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(hopper.getCreatorUUID());
        ArrayList<Material> fullList = new ArrayList<>();
        for(FilterItem item : hopper.getItems()){
            fullList.add(item.getMaterial());
        }
        if(HopperFilter.getPerms().playerHas(Bukkit.getServer().getWorlds().get(0).getName(), offlinePlayer, Permissions.HOPPER_SLOT_5.getNode())){
            return fullList;
        }if(HopperFilter.getPerms().playerHas(Bukkit.getServer().getWorlds().get(0).getName(), offlinePlayer, Permissions.HOPPER_SLOT_4.getNode())){
            fullList.remove(fullList.size()-1);
            return fullList;
        }
        fullList.remove(fullList.size()-1);
        fullList.remove(fullList.size()-1);
        return fullList;
    }

    @EventHandler
    public void onHopperPickupFromContainer(InventoryPickupItemEvent event) {
        FilterHopper hopper = HopperFilter.getInstance().getFilterHopperMap().get(FilterHopper.createUUIDFromLocation(event.getInventory().getLocation()));
        if(hopper != null){
            if(hopper.isEnabled()){
                Material material = event.getItem().getItemStack().getType();
                if(hopper.isWhitelist()){
                    if(!getPermissionbasedBlackList(hopper).contains(material)){
                        event.setCancelled(true);
                    }
                } else {
                    if(hopper.containsMaterial(material)){
                        if(hasPermissionToBeFiltered(hopper, hopper.getSlotOfMaterial(material)))
                            event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHopperPickupFromContainer(InventoryMoveItemEvent event) {
        if(event.getInitiator().getType().equals(InventoryType.HOPPER)){
            FilterHopper hopper = HopperFilter.getInstance().getFilterHopperMap().get(FilterHopper.createUUIDFromLocation(event.getInitiator().getLocation()));
            if(hopper != null){
                if(hopper.isEnabled()){
                    Material material = event.getItem().getType();
                    if(hopper.isWhitelist()){
                        if(!getPermissionbasedBlackList(hopper).contains(material)){
                            event.setCancelled(true);
                        }
                    } else {
                        if(hopper.containsMaterial(material)){
                            if(hasPermissionToBeFiltered(hopper, hopper.getSlotOfMaterial(material)))
                                event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }


}
