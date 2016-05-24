package com.brianyarr.jaws.region;

import com.amazonaws.regions.Regions;

import java.util.Optional;

public class RegionUtil {

    private RegionUtil() {

    }

    public static Optional<Regions> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        name = name.trim();

        for (Regions r : Regions.values()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

}
