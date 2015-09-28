package org.vm.evarianttest.filters;

import org.vm.evarianttest.entity.weather.WBANRainfallRecord;

import java.util.function.Predicate;

/**
 * This class represents a filter that can be applied on Rainfall dataset. It filters the May 2015 records.
 *
 * @author vivekm
 * @since 1.0
 */
public class RainFallFileMay2015PeriodIncludeFilter implements Predicate<WBANRainfallRecord> {
    //TODO: Use Data instead of String
    private static final String MAY_2015_INDICATOR = "201505";
    @Override
    public boolean test(WBANRainfallRecord wbanRainfallRecord) {
        return wbanRainfallRecord.getYearMonthDay().contains(MAY_2015_INDICATOR);
    }
}
