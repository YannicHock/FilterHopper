package de.boom.hopperFilter.mysql.dataobjects;

import org.bukkit.Material;

public class FilterItem {

    private final Material material;

    public FilterItem(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
