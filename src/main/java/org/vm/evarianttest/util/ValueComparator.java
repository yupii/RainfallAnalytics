package org.vm.evarianttest.util;

import java.util.Comparator;
import java.util.Map;

/**
 * This comparator compares the values of the map. Maps by default use keys for sorting, but using this approach as the problem asks for sorted by aggregated rainfall amount.
 *
 * @author vivekm
 * @since 1.0
 */
class ValueComparator<K, V> implements Comparator {
    Map<K, V> map;
    public ValueComparator(Map<K, V> map) {
        this.map = map;
    }
    public int compare(Object keyA, Object keyB) {
        Comparable valueA = (Comparable) map.get(keyA);
        Comparable valueB = (Comparable) map.get(keyB);
        return valueB.compareTo(valueA);
    }
}
