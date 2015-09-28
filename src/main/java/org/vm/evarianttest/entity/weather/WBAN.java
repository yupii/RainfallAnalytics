package org.vm.evarianttest.entity.weather;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;
import org.vm.evarianttest.entity.census.USACity;
import org.vm.evarianttest.entity.census.USACounty;
import org.vm.evarianttest.entity.census.USAStatisticalArea;
import org.vm.evarianttest.util.Util;

/**
 * This class represents a Weather-Bureau-Army-Navy (WBAN) station which is linked to a Statistical Area.
 *
 * @author vivekm
 * @since 1.0
 */
public class WBAN implements Comparable, KeyAware, Entity {

    private Key wbanIdKey;
    private String wbanStationName;

    private USACity cityName;
    private String stationLocatedAt;
    private USACounty county;
    private USAStatisticalArea area;

    public WBAN(String wbanId, String wbanStationName) {
        wbanIdKey = new Key(Util.cleanString(wbanId));
        this.wbanStationName = Util.cleanString(wbanStationName);
    }

    public String getWbanStationName() {
        return wbanStationName;
    }

    public void setWbanStationName(String wbanStationName) {
        this.wbanStationName = wbanStationName;
    }

    public USACity getCityName() {
        return cityName;
    }

    public void setCityName(USACity cityName) {
        this.cityName = cityName;
    }

    public String getStationLocatedAt() {
        return stationLocatedAt;
    }

    public void setStationLocatedAt(String stationLocatedAt) {
        this.stationLocatedAt = stationLocatedAt;
    }

    public USACounty getCounty() {
        return county;
    }

    public void setCounty(USACounty county) {
        this.county = county;
    }

    public USAStatisticalArea getArea() {
        return area;
    }

    public void setArea(USAStatisticalArea area) {
        this.area = area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WBAN wban = (WBAN) o;

        if (!wbanIdKey.equals(wban.wbanIdKey)) return false;
        return wbanStationName.equals(wban.wbanStationName);

    }

    @Override
    public int hashCode() {
        int result = wbanIdKey.hashCode();
        result = 31 * result + wbanStationName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WBAN{");
        sb.append("wbanIdKey=").append(wbanIdKey);
        sb.append(", wbanStationName='").append(wbanStationName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return String.valueOf(wbanIdKey).compareTo(String.valueOf(o));
    }

    @Override
    public Key key() {
        return this.wbanIdKey;
    }
}
