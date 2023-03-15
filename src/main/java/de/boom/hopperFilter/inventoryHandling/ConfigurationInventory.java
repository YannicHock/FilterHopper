package de.boom.hopperFilter.inventoryHandling;

import de.boom.hopperFilter.HopperFilter;
import de.boom.hopperFilter.callbacks.LoadSingleHopperCallback;
import de.boom.hopperFilter.configuration.Permissions;
import de.boom.hopperFilter.configuration.StringTable;
import de.boom.hopperFilter.mysql.MySQLAccessor;
import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ConfigurationInventory implements Listener {

    public void updateItems(UUID openerUUID, Inventory inv) {
        CustomInventoryHolder holder = ((CustomInventoryHolder)inv.getHolder());
        holder.setLoading(true);
        ItemStack placeholderItem_loading = createGuiItem(Material.WHITE_STAINED_GLASS_PANE, StringTable.INVENTORYITEM_LOADING_NAME.getComponentBuilder(), null);

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, placeholderItem_loading);
        }
        loadInformation(openerUUID, ((CustomInventoryHolder)inv.getHolder()).getUUID(), result -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(HopperFilter.getInstance().getFilterHopperMap().get(holder.getUUID()).getCreatorUUID());
            ItemStack placeholderItem_loaded = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(""), null);

            //first row
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, placeholderItem_loaded);
            }

            //second row
            for (int i = 0; i < 9; i++) {
                int j = 9+i;
                if(j == 12){
                    if(result.isEnabled()){
                        inv.setItem(j, createGuiItem(Material.BARRIER, StringTable.INVENTORYITEM_DISABLEHOPPER_NAME.getComponentBuilder(), null));
                    } else {
                        inv.setItem(j, createGuiItem(Material.EMERALD, StringTable.INVENTORYITEM_ENABLEHOPPER_NAME.getComponentBuilder(), null));
                    }
                    continue;
                }
                if(j == 14){
                    if(result.isWhitelist()){
                        inv.setItem(j, createGuiItem(Material.CHEST_MINECART, StringTable.INVENTORYITEM_WHITELIST_NAME.getComponentBuilder(), StringTable.INVENTORYITEM_WHITELIST_LORE.getComponentListBuilder()));
                    } else {
                        inv.setItem(j, createGuiItem(Material.TNT_MINECART, StringTable.INVENTORYITEM_BLACKLIST_NAME.getComponentBuilder(), StringTable.INVENTORYITEM_BLACKLIST_LORE.getComponentListBuilder()));
                    }
                    continue;
                }
                inv.setItem(j, placeholderItem_loaded);
            }


            //third Row
            for (int i = 0; i < 9; i++) {
                inv.setItem(18+i, placeholderItem_loaded);
            }

            //fourth row
            for (int i = 0; i < 9; i++) {
                int j = 27+i;
                if(j > 28 && j < 34){
                    int index = j-29;
                    boolean blocked = false;
                    switch (index) {
                        case 3 -> {
                            if (!HopperFilter.getPerms().playerHas(Bukkit.getServer().getWorlds().get(0).getName(), offlinePlayer, Permissions.HOPPER_SLOT_4.getNode())) {
                                blocked = true;
                                inv.setItem(j,
                                        createGuiItem(Material.BARRIER, StringTable.INVENTORYITEM_LOCKEDSLOT_NAME.getComponentBuilder(),
                                                StringTable.INVENTORYITEM_LOCKEDSLOT_LORE.getComponentListWithReplacement("%needed_rank%", StringTable.PERMISSION_RANKMAP_SLOT_4.getRawtext())));
                            }
                        }
                        case 4 -> {
                            if (!HopperFilter.getPerms().playerHas(Bukkit.getServer().getWorlds().get(0).getName(), offlinePlayer, Permissions.HOPPER_SLOT_5.getNode())) {
                                blocked = true;
                                inv.setItem(j,
                                        createGuiItem(Material.BARRIER, StringTable.INVENTORYITEM_LOCKEDSLOT_NAME.getComponentBuilder(),
                                                StringTable.INVENTORYITEM_LOCKEDSLOT_LORE.getComponentListWithReplacement("%needed_rank%", StringTable.PERMISSION_RANKMAP_SLOT_5.getRawtext())));
                            }
                        }
                        default -> {
                        }
                    }
                    if(!blocked){
                        if(result.getItems().size()-1 >= index && !result.getItems().isEmpty()){
                            inv.setItem(j, new ItemStack(result.getItems().get(index).getMaterial()));
                        } else {
                            inv.setItem(j, new ItemStack(Material.AIR));
                        }
                    }
                    continue;
                }
                inv.setItem(j, placeholderItem_loaded);
            }

            //fifth Row
            for (int i = 0; i < 9; i++) {
                inv.setItem(36+i, placeholderItem_loaded);
            }
            ((CustomInventoryHolder)inv.getHolder()).setLoading(false);
        });
    }


    public static void loadInformation(UUID openerUUID, String uuid, final LoadSingleHopperCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(HopperFilter.getInstance(), () -> {
            FilterHopper result = HopperFilter.getInstance().getFilterHopperMap().get(uuid);

            if(result == null){
                result = MySQLAccessor.createHopper(openerUUID, uuid);
            }
            final FilterHopper finalResult = result;
            Bukkit.getScheduler().runTask(HopperFilter.getInstance(), () -> callback.onQueryDone(finalResult));
        });
    }


    protected ItemStack createGuiItem(final Material material, final TextComponent name, final List<Component> lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.displayName(name);

        if(lore != null){
            meta.lore(lore);
        }

        item.setItemMeta(meta);

        return item;
    }

    public void openInventory(final HumanEntity ent, Location loc) {
        if(!(ent instanceof final Player player))
            return;

        Inventory inv = new CustomInventoryHolder(FilterHopper.createUUIDFromLocation(loc)).getInventory();

        updateItems(player.getUniqueId(), inv);
        player.openInventory(inv);
    }


    private boolean isInTopInventorySpaces(Inventory clickedInventory, Inventory topInventory, int slot){
        return clickedInventory.equals(topInventory) && slot > 28 && slot < 34;
    }

    private boolean isInBottomInventory(Inventory clickedInventory, Inventory bottimInventory){
        return clickedInventory.equals(bottimInventory);
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof final Player p))
            return;

        if (!(e.getView().getTopInventory().getHolder() instanceof CustomInventoryHolder inventoryHolder))
            return;

        if(inventoryHolder.isLoading()){
            e.setCancelled(true);
        }


        if(e.getClickedInventory() == null)
            return;

        if(isInBottomInventory(e.getClickedInventory(), e.getView().getBottomInventory()) && !e.getClick().isShiftClick()){
            return;
        }

        FilterHopper hopper = HopperFilter.getInstance().getFilterHopperMap().get(inventoryHolder.getUUID());

        if((isInTopInventorySpaces(e.getClickedInventory(), e.getView().getTopInventory(), e.getSlot()) && e.getCurrentItem() != null || e.getCursor() != null) || (isInBottomInventory(e.getClickedInventory(), e.getView().getBottomInventory()) && e.getClick().isShiftClick())){
            try {
                ItemStack stack = null;
                InventoryAction action = e.getAction();
                switch (action){
                    case PLACE_ALL:
                    case PLACE_ONE:
                    case PLACE_SOME:
                        try {
                            hopper.addItem(e.getCursor().getType());
                        } catch (SQLException ex){
                            e.setCancelled(true);
                            ex.printStackTrace();
                            return;
                        }
                        break;
                    case PICKUP_ALL:
                    case PICKUP_ONE:
                    case PICKUP_HALF:
                    case PICKUP_SOME:
                        try {
                            hopper.removeItem(e.getSlot() -29);
                        } catch (SQLException ex){
                            e.setCancelled(true);
                            ex.printStackTrace();
                            return;
                        }
                        break;
                    case MOVE_TO_OTHER_INVENTORY:
                        if(inventoryHolder.getInventory().equals(e.getClickedInventory())){
                            try {
                                hopper.removeItem(e.getSlot()-29);
                            } catch (SQLException ex){
                                e.setCancelled(true);
                                ex.printStackTrace();
                                return;
                            }
                        } else {
                            try {
                                hopper.addItem(e.getCurrentItem().getType());
                                stack = e.getCurrentItem();
                            } catch (SQLException ex){
                                e.setCancelled(true);
                                ex.printStackTrace();
                                return;
                            }
                        }
                    default:
                        break;
                }
                updateItems(p.getUniqueId(), inventoryHolder.getInventory());
                e.setCurrentItem(new ItemStack(Material.AIR));
                if(stack != null){
                    e.getView().getBottomInventory().setItem(e.getSlot(),stack);
                }
                return;
            } catch (IndexOutOfBoundsException ignore){}
        }

        //Enable/Disable
        if(e.getSlot() == 12){
            try {
                hopper.setEnabled(!hopper.isEnabled());
                updateItems(p.getUniqueId(), inventoryHolder.getInventory());
            } catch (SQLException ex){
                e.setCancelled(true);
                ex.printStackTrace();
                return;
            }
        }

        //Whitelist/Blacklist
        if(e.getSlot() == 14){
            try {
                hopper.setWhitelist(!hopper.isWhitelist());
                updateItems(p.getUniqueId(), inventoryHolder.getInventory());
            } catch (SQLException ex) {
                e.setCancelled(true);
                ex.printStackTrace();
                return;
            }
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f,  1f);
            if(hopper.isWhitelist()){
                p.sendMessage(StringTable.MESSAGES_CHANGED_FILTERTYPE.getComponentWithReplacement("%Filtermodus%", "Erlaubte Items"));
            } else {
                p.sendMessage(StringTable.MESSAGES_CHANGED_FILTERTYPE.getComponentWithReplacement("%Filtermodus%", "Verbotene Items"));
            }
        }

        e.setCancelled(true);

    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().getViewers().contains(e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }
}
