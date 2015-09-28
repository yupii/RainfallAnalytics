package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a US City with City Name and State to which it belongs to, in the context of Census. Only Metropolitan areas will span more than one State, not cities.
 * ref: http://ask.metafilter.com/23195/Cities-that-span-two-or-more-states
 *
 * KeyFormat for this entity: {CityName,State Abbr} in uppercase. Eg: AUSTIN,TX
 *
 * @author Vivekm
 * @since 1.0
 */
public class USACity implements Comparable, KeyAware, Entity {
    private Key key;
    private String name;
    private USAState state = USAState.UNKNOWN;
    private Map<Key, USAStatisticalArea> partOfAreas = new LinkedHashMap<>();

    /**
     * Constructor
     * @param name
     * @param state
     */
    public USACity(final String name, final USAState state) {
        this.name = name.trim().toUpperCase();
        if(state != null){
            this.state = state;
        }
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

    public void addPartOfArea(USAStatisticalArea partOfArea){
        getPartOfAreas().put(partOfArea.key(), partOfArea);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        USACity usaCity = (USACity) o;

        if (!name.equals(usaCity.name)) return false;
        return state == usaCity.state;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((USACity) o).getName());
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("USACity{");
        sb.append("key=").append(key.toString());
        sb.append(", name='").append(name).append('\'');
        sb.append(", state=").append(state.getAbbreviation());
        sb.append('}');
        return sb.toString();
    }
}