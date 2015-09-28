package org.vm.evarianttest.filters;

import org.vm.evarianttest.entity.weather.WBANRainfallRecord;

import java.util.function.Predicate;

/**
 * This class represents a filter that can be applied on Rainfall dataset. It filters records that are from Mid Night, represented by 0 hrs, to 7 AM.
 * TODO: Filter could be made using DateTime instead of number, since Dataset is clean it works.
 *
 * @author vivekm
 * @since 1.0
 */
public class RainFallFileMidNightTo7AMHourWindowExcludeFilter implements Predicate<WBANRainfallRecord>{
    @Override
    public boolean test(WBANRainfallRecord wbanRainfallRecord) {
        return (wbanRainfallRecord.getHour() > 7);
    }
}
