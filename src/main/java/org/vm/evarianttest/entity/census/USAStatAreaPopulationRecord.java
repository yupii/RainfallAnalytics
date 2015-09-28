package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;

import java.time.Year;

/**
 * Created by Vivek M on 9/26/2015.
 */
public class USAStatAreaPopulationRecord implements Entity, KeyAware, Comparable{
    private USAStatisticalArea area;

    private long basePopulation;
    private Year baseYear;

    private long currentPopulation;
    private Year currentPopYear;

    private double averageYearlyPopChangeRate;

    public USAStatisticalArea getArea() {
        return area;
    }

    public void setArea(USAStatisticalArea area) {
        this.area = area;
    }

    public long getBasePopulation() {
        return basePopulation;
    }

    public void setBasePopulation(long basePopulation) {
        this.basePopulation = basePopulation;
    }

    public Year getBaseYear() {
        return baseYear;
    }

    public void setBaseYear(Year baseYear) {
        this.baseYear = baseYear;
    }

    public long getCurrentPopulation() {
        return currentPopulation;
    }

    public void setCurrentPopulation(long currentPopulation) {
        this.currentPopulation = currentPopulation;
    }

    public Year getCurrentPopYear() {
        return currentPopYear;
    }

    public void setCurrentPopYear(Year currentPopYear) {
        this.currentPopYear = currentPopYear;
    }

    public double getAverageYearlyPopChangeRate() {
        return averageYearlyPopChangeRate;
    }

    public void setAverageYearlyPopChangeRate(double averageYearlyPopChangeRate) {
        this.averageYearlyPopChangeRate = averageYearlyPopChangeRate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("USAStatAreaPopulationRecord{");
        sb.append("area=").append(area.toString());
        sb.append(", basePopulation=").append(basePopulation);
        sb.append(", baseYear=").append(baseYear);
        sb.append(", currentPopulation=").append(currentPopulation);
        sb.append(", currentPopYear=").append(currentPopYear);
        sb.append(", averageYearlyPopChangeRate=").append(averageYearlyPopChangeRate);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        USAStatAreaPopulationRecord that = (USAStatAreaPopulationRecord) o;

        if (basePopulation != that.basePopulation) return false;
        if (currentPopulation != that.currentPopulation) return false;
        if (Double.compare(that.averageYearlyPopChangeRate, averageYearlyPopChangeRate) != 0) return false;
        if (area != null ? !area.equals(that.area) : that.area != null) return false;
        if (baseYear != null ? !baseYear.equals(that.baseYear) : that.baseYear != null) return false;
        return !(currentPopYear != null ? !currentPopYear.equals(that.currentPopYear) : that.currentPopYear != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = area != null ? area.hashCode() : 0;
        result = 31 * result + (int) (basePopulation ^ (basePopulation >>> 32));
        result = 31 * result + (baseYear != null ? baseYear.hashCode() : 0);
        result = 31 * result + (int) (currentPopulation ^ (currentPopulation >>> 32));
        result = 31 * result + (currentPopYear != null ? currentPopYear.hashCode() : 0);
        temp = Double.doubleToLongBits(averageYearlyPopChangeRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public Key key() {
        return this.area.key();
    }

    @Override
    public int compareTo(Object o) {
        return this.getArea().compareTo(((USAStatAreaPopulationRecord)o).getArea());
    }
}
