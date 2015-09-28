package org.vm.evarianttest.filters;

import org.vm.evarianttest.entity.weather.WBANRainfallRecord;

import java.util.function.Predicate;

/**
 * This class represents a filter that can be applied on Rainfall dataset. It filters the header record.
 *
 * @author vivekm
 * @since 1.0
 */
public class RainFallExcludeFileHeaderFilter implements Predicate<String>{
    @Override
    public boolean test(String s) {
        return !s.contains(WBANRainfallRecord.FILE_HEADER);
    }
}
