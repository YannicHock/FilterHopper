package de.boom.hopperFilter.callbacks;

import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;

import java.util.HashMap;

public interface LoadAllHopperCallback {

    void onQueryDone(HashMap<String, FilterHopper> result);

}
