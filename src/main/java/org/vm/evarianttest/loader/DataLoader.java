package org.vm.evarianttest.loader;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;

import java.util.Map;

/**
 * This interface defines the contract for a Data Load operation. Instead of load() returning the loaded the data, we are defining a separate method
 * getDataMaps() to support use cases like Database load etc.
 *
 * Example usage:
 *
 * DataLoader loader = new XXXDataLoader(...);
 * loader.load()
 * List maps = loader.getDataMaps();
 *
 * @author vivekm
 * @since 1.0
 * @see KeyAware
 * @see Entity
 */
public interface DataLoader <K extends Key,V extends Entity> {
    /**
     * This method loads the data in the data loader process. It may read one or more inputs and load one or more data sources.
     *
     * @throws DataLoaderException - In case of any load failures
     */
    public void load() throws DataLoaderException;

    /**
     * This method returns the data loaded by the loaded, as a list of maps that are entites in the current system. There is no contractual binding to call load() before getDataMaps()
     * and it is up to the implementing classes to decide.
     *
     * @return - Map of Maps containing Entity objects.
     * @throws DataLoaderException - In case of any load failures
     */
    public Map<String, Map<K ,V>> getDataMaps() throws DataLoaderException;
}