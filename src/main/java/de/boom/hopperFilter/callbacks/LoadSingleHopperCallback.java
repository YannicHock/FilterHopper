package de.boom.hopperFilter.callbacks;

import de.boom.hopperFilter.mysql.dataobjects.FilterHopper;

public interface LoadSingleHopperCallback {

    void onQueryDone(FilterHopper result);

}
