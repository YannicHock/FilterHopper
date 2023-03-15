package de.boom.hopperFilter.configuration;

import de.boom.hopperFilter.HopperFilter;
import org.bukkit.configuration.file.FileConfiguration;

public enum DefaultConfig {
    DATABASE_HOST("database.host"),
    DATABASE_PORT("database.port"),
    DATABASE_DATABASENAME("database.name"),
    DATABASE_USERNAME("database.username"),
    DATABASE_PASSWORD("database.password"),
    ;

    private final String node;
    private static FileConfiguration config;


    DefaultConfig(String node){
        this.node = node;
    }

    public static void load(HopperFilter instance) {
        instance.saveDefaultConfig();
        config = instance.getConfig();
    }

    public String getString(){
        return config.getString(this.node);
    }

    public boolean getBoolean(){
        return config.getBoolean(this.node);
    }

    public int getInt(){
        return config.getInt(this.node);
    }
}