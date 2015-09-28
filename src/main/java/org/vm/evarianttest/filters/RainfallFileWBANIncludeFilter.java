package org.vm.evarianttest.filters;

import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.util.Util;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class represents a filter that can be applied on Rainfall dataset. It filters records for the input WBAN Ids.
 *
 * @author vivekm
 * @since 1.0
 */
public class RainfallFileWBANIncludeFilter implements Predicate<String> {
    private Map<Key, List<Key>> wbansToInclude;
    public RainfallFileWBANIncludeFilter(Map<Key, List<Key>> wbansToInclude){
        this.wbansToInclude = wbansToInclude;
    }
    @Override
    public boolean test(String s) {
        String[] inputArr = s.split(",");
        if(inputArr != null && inputArr.length > 0){
            Key wbanProspect = new Key(Util.cleanString(inputArr[0]));
            return wbansToInclude.containsKey(wbanProspect);
        }
        return false;
    }
}
