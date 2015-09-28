package org.vm.evarianttest.entity.weather;

import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;
import org.vm.evarianttest.util.Util;

/**
 * This class represents the rainfall record as found in the QCLCD files.
 *
 * @author vivekm
 * @since 1.0
 */
public class WBANRainfallRecord implements KeyAware{
    public static final String FILE_HEADER = "Wban,YearMonthDay,Hour,Precipitation,PrecipitationFlag";
    public static final String TRACE_AMT_INDICATOR = "T";
    public static final String INPUT_DATE_PATTERN = "yyyymmdd";

    private String wbanId;
    private Key rainFallRecKey;
    private String yearMonthDay;
    private int hour;
    private double precipitation;
    private String precipitationFlag;

    private WBANRainfallRecord(String wbanId, String yearMonthDay, int hour, double precipitation, String precipitationFlag) {
        this.wbanId = wbanId;
        //TODO : Change the Date to include Hours as well, so it will be easy to check
        this.yearMonthDay = yearMonthDay;
        this.hour = hour;
        this.precipitation = precipitation;
        this.precipitationFlag = precipitationFlag;
        this.rainFallRecKey = new Key(wbanId + "-" + yearMonthDay + "-" + hour);
    }

    public String getWbanId() {
        return wbanId;
    }

    public String getYearMonthDay() {
        return yearMonthDay;
    }

    public int getHour() {
        return hour;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public String getPrecipitationFlag() {
        return precipitationFlag;
    }

    public static WBANRainfallRecord build(String record){
        String[] inputArr = record.split(",");
        String wbanId = Util.cleanString(inputArr[0]);
        String yearMonthDay = Util.cleanString(inputArr[1]);
        int hour = Integer.parseInt(Util.cleanString(inputArr[2]));
        double precipitationAmt = (inputArr[3] != null && !inputArr[3].trim().isEmpty() && !Util.cleanString(inputArr[3]).contains(TRACE_AMT_INDICATOR)) ? Double.parseDouble(inputArr[3]) : 0.0d;
        String precipitationFlag = Util.cleanString(inputArr[4]);

        return new WBANRainfallRecord(wbanId, yearMonthDay, hour, precipitationAmt, precipitationFlag);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WBANRainfallRecord{");
        sb.append("wbanId='").append(wbanId).append('\'');
        sb.append(", yearMonthDay='").append(yearMonthDay).append('\'');
        sb.append(", hour=").append(hour);
        sb.append(", precipitation=").append(precipitation);
        sb.append(", PrecipitationFlag='").append(precipitationFlag).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WBANRainfallRecord that = (WBANRainfallRecord) o;

        if (hour != that.hour) return false;
        if (Double.compare(that.precipitation, precipitation) != 0) return false;
        if (!wbanId.equals(that.wbanId)) return false;
        if (!yearMonthDay.equals(that.yearMonthDay)) return false;
        return !(precipitationFlag != null ? !precipitationFlag.equals(that.precipitationFlag) : that.precipitationFlag != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = wbanId.hashCode();
        result = 31 * result + yearMonthDay.hashCode();
        result = 31 * result + hour;
        temp = Double.doubleToLongBits(precipitation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (precipitationFlag != null ? precipitationFlag.hashCode() : 0);
        return result;
    }

    @Override
    public Key key() {
        return rainFallRecKey;
    }
}
