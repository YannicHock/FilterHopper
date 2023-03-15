package de.boom.hopperFilter.configuration;

import de.boom.hopperFilter.HopperFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum StringTable {
    MESSAGES_CHANGED_FILTERTYPE("messages.changedFiltertype"),
    INVENTORYNAME_CONFIG_MENU("inventoryNames.configurationMenu"),
    INVENTORYITEM_LOADING_NAME("inventoryItems.loading.name"),
    INVENTORYITEM_DISABLEHOPPER_NAME("inventoryItems.disableHopper.name"),
    INVENTORYITEM_ENABLEHOPPER_NAME("inventoryItems.enableHopper.name"),
    INVENTORYITEM_WHITELIST_NAME("inventoryItems.whitelist.name"),
    INVENTORYITEM_WHITELIST_LORE("inventoryItems.whitelist.lore"),
    INVENTORYITEM_BLACKLIST_NAME("inventoryItems.blacklist.name"),
    INVENTORYITEM_BLACKLIST_LORE("inventoryItems.blacklist.lore"),
    INVENTORYITEM_LOCKEDSLOT_NAME("inventoryItems.lockedSlot.name"),
    INVENTORYITEM_LOCKEDSLOT_LORE("inventoryItems.lockedSlot.lore"),

    PERMISSION_RANKMAP_SLOT_4("permission.rankMap.slot_4"),
    PERMISSION_RANKMAP_SLOT_5("permission.rankMap.slot_5"),
    ;

    private final String node;
    private static FileConfiguration config;

    StringTable(String node){
        this.node = node;
    }

    public static void load(HopperFilter instance) {
        instance.saveResource("stringtable.yml", false);
        File messageFile = new File(instance.getDataFolder(), "stringtable.yml");
        config = YamlConfiguration.loadConfiguration(messageFile);
    }


    public TextComponent getComponentBuilder(){
        return LegacyComponentSerializer.legacySection().deserialize(config.getString(this.node));
    }

    public TextComponent getComponentWithReplacement(String regEx, String replacement){
        String raw = this.getRawtext().replaceAll(regEx, replacement);
        return LegacyComponentSerializer.legacySection().deserialize(raw);
    }

    private TextComponent getComponentWithReplacement(String raw, String regEx, String replacement){
        raw = raw.replaceAll(regEx, replacement);
        return LegacyComponentSerializer.legacySection().deserialize(raw);
    }


    public List<Component> getComponentListBuilder(){
        List<Component> list = new ArrayList<>();
        for(String s : config.getStringList(this.node)){
            list.add(LegacyComponentSerializer.legacySection().deserialize(s));
        }
        return list;
    }

    public List<String> getRawList(){
        return config.getStringList(this.node);
    }

    public String getRawtext(){
        return config.getString(this.node);
    }

    public List<Component> getComponentListWithReplacement(String regEx, String replacement){
        List<Component> replacedComponents = new ArrayList<>();
        for(String raw : this.getRawList()){
            replacedComponents.add(getComponentWithReplacement(raw, regEx, replacement));
        }
        return replacedComponents;
    }

}
