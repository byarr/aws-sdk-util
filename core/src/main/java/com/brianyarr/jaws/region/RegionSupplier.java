package com.brianyarr.jaws.region;

import com.amazonaws.regions.Regions;

public interface RegionSupplier {

    Regions getRegion();
}
