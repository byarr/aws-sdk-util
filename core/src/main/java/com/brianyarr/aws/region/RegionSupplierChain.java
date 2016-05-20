package com.brianyarr.aws.region;

import com.amazonaws.regions.Regions;

import java.util.Arrays;

public class RegionSupplierChain implements RegionSupplier {

    private final RegionSupplier[] suppliers;

    public RegionSupplierChain(final RegionSupplier... suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public Regions getRegion() {
        return Arrays.stream(suppliers).map(RegionSupplier::getRegion).filter(r -> r != null).findFirst().orElse(null);
    }
}
