package com.brianyarr.aws.region;

import com.amazonaws.regions.Regions;

public interface RegionSupplier {

    Regions getRegion();
}
