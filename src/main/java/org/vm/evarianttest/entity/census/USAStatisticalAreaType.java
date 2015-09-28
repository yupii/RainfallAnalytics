package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * This Typelist contains list of all Statistical areas in USA. Currently they are Metropolitan and Micropolitan area. This is based on the population of that area.
 * For more info ref: http://www.census.gov/population/metro/
 *
 * This enumeration also support operation like findByName().
 *
 * @author vivekm
 * @since 1.0
 */
public enum USAStatisticalAreaType implements Entity{
    METROPOLITAN_STATISTICAL_AREA ("Metropolitan statistical area"),
    MICROPOLITAN_STATISTICAL_AREA ("Micropolitan statistical area");

    private final String type;
    private static final Map<String, USAStatisticalAreaType> AREA_TYPE_BY_NAME = new HashMap<>();
    static{
        for(USAStatisticalAreaType areaType : values()) {
            AREA_TYPE_BY_NAME.put(areaType.type, areaType);
        }
    }
    USAStatisticalAreaType(String type) {
        this.type = type;
    }

    public static USAStatisticalAreaType findByName(String name){
        return AREA_TYPE_BY_NAME.get(name.trim());
    }

    public static boolean isValid(String name) {
        return AREA_TYPE_BY_NAME.containsKey(name);
    }
}