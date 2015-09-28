package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;
import org.vm.evarianttest.entity.weather.WBAN;
import org.vm.evarianttest.util.Util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This entity represents a Statistical Area in USA as defined by Census department. It can be a Micropolitan or Metropolitan area as defined by the Typelist
 * USAStatisticalAreaType.
 *
 * ref: http://www.census.gov/population/metro/data/def.html
 */
public class USAStatisticalArea implements KeyAware, Entity, Comparable {
    private Key key;
    private int areaId;
    private String name;
    private USAStatisticalAreaType type;
    private boolean isPuertoRicoArea;

    private Map<Key, USACity> cities = new LinkedHashMap<>();
    private Map<Key, USAState> states = new LinkedHashMap<>();
    private Map<Key, USACounty> counties = new LinkedHashMap<>();

    private Map<Key, WBAN> weatherStations = new LinkedHashMap<>();

    public USAStatisticalArea(final int areaId, final String name, final USAStatisticalAreaType type, final boolean isPuertoRicoArea) {
        this.areaId = areaId;
        this.name = name;
        this.type = type;
        init(name, isPuertoRicoArea);
    }

    private void init(final String name, final boolean isPuertoRicoArea){
        List<String> extractedCityNames = Util.getCleanCityNames(name);
        List<String> extractedStateNames = Util.getCleanStateNames(name);

        String keyStr =Util.makeKey(extractedCityNames, extractedStateNames);
        this.key = new Key(keyStr);

        for(String cityName : extractedCityNames){
            USACity city = new USACity(cityName.trim(), null);
            city.addPartOfArea(this);
            cities.put(city.key(), city);
        }

        for(String stateCode : extractedStateNames){
            USAState state = USAState.findByAbbreviation(stateCode);
            states.put(new Key(state.getAbbreviation()), state);
        }

        //For now we just assume it but we can add validation using state values - vm
        this.isPuertoRicoArea = isPuertoRicoArea;
    }

    @Override
    public Key key(){
        return this.key;
    }

    public int getAreaId() {
        return areaId;
    }

    public String getName() {
        return name;
    }

    public USAStatisticalAreaType getType() {
        return type;
    }

    public Map<Key, USACity> getCities() {
        return cities;
    }

    public void setCities(Map<Key, USACity> cities) {
        this.cities = cities;
    }

    public Map<Key, USAState> getStates() {
        return states;
    }

    public void setStates(Map<Key, USAState> states) {
        this.states = states;
    }

    public Map<Key, USACounty> getCounties() {
        return counties;
    }

    public void setCounties(Map<Key, USACounty> counties) {
        this.counties = counties;
    }

    public Map<Key, WBAN> getWeatherStations() {
        return weatherStations;
    }

    public void setWeatherStations(Map<Key, WBAN> weatherStations) {
        this.weatherStations = weatherStations;
    }

    public void addCity(USACity city){
        getCities().put(city.key(), city);
    }

    public void addCounty(USACounty county){
        getCounties().put(county.key(), county);
    }

    public void addWeatherStation(WBAN wban){
        getWeatherStations().put(wban.key(), wban);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        USAStatisticalArea that = (USAStatisticalArea) o;

        if (areaId != that.areaId) return false;
        if (isPuertoRicoArea != that.isPuertoRicoArea) return false;
        if (!key.equals(that.key)) return false;
        if (!name.equals(that.name)) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + areaId;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (isPuertoRicoArea ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("USAStatisticalArea{");
        sb.append(", key=").append(key.toString());
        sb.append("cities=").append(cities.toString());
        sb.append(", states=").append(states.toString());
        sb.append(", counties=").append(counties.toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((USAStatisticalArea)o).getName());
    }
}