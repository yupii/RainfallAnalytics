package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;
import org.vm.evarianttest.entity.weather.WBAN;

import java.util.*;

/**
 * This class represents a US City with City Name and State to which it belongs to, in the context of Census. Only Metropolitan areas will span more than one State, not cities.
 * ref: http://ask.metafilter.com/23195/Cities-that-span-two-or-more-states
 *
 * @author Vivekm
 * @since 1.0
 */
public class USACounty implements Comparable, KeyAware, Entity {
    private Key key;
    private String name;
    private USAState state;
    private Map<Key, USAStatisticalArea> partOfAreas = new LinkedHashMap<>();
    private Map<Key, WBAN> weatherStations = new LinkedHashMap<>();

    public USACounty(final String name, final USAState state) {
        this.name = name.toUpperCase();
        this.state = state;
        key = new Key(getName() + "," + getState().getAbbreviation());
    }

    public String getName() {
        return name;
    }

    public USAState getState() {
        return state;
    }

    public Map<Key, USAStatisticalArea> getPartOfAreas() {
        return partOfAreas;
    }

    public void setPartOfAreas(Map<Key, USAStatisticalArea> partOfAreas) {
        this.partOfAreas = partOfAreas;
    }

    public Map<Key, WBAN> getWeatherStations() {
        return weatherStations;
    }

    public void setWeatherStations(Map<Key, WBAN> weatherStations) {
        this.weatherStations = weatherStations;
    }

    public void addPartOfArea(USAStatisticalArea partOfArea){
        getPartOfAreas().put(partOfArea.key(), partOfArea);
    }

    public void addWeatherStation(WBAN wban){
        getWeatherStations().put(wban.key(), wban);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        USACounty usaCounty = (USACounty) o;
        return this.key.equals(usaCounty.key());

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("USACounty{");
        sb.append("name='").append(name).append('\'');
        sb.append(", state=").append(state.getAbbreviation());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((USACounty)o).getName() );
    }

    @Override
    public Key key() {
        return key;
    }
}