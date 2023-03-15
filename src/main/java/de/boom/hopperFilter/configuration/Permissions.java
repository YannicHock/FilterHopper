package de.boom.hopperFilter.configuration;
public enum Permissions {
    HOPPER_SLOT_4("slot.4"),
    HOPPER_SLOT_5("slot.5"),
    ;

    private final String node;

    Permissions(String node){
        this.node = node;
    }


    public String getNode(){
        String prefix = "hopperfilter.";
        return prefix + node;
    }

}
