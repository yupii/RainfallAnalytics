package org.vm.evarianttest.calc;

import org.vm.evarianttest.entity.Key;

import java.util.Map;

/**
 * This interface defines some sort of computational operation in the system using system Entites, resulting in a computed value as a Map.
 *
 * @author vivekm
 * @since 1.0
 */
public interface Calculator <K, V> {
    /**
     * This method performs a calculation and returns the result as a Map.Example use cases include returning an aggregated value for a Map.
     *
     * @return Map containing results
     */
    public Map<Key, Object> calculate();

    /**
     * This method performs a calculation and retusn the result as a single Object. Example computing a sum of two integers.
     *
     * @return
     */
    public Object calculateSingleValue();

    /**
     * This method formats the result and returns in a printable format.
     *
     * @return
     */
    public StringBuilder getPrettyResult();
}
