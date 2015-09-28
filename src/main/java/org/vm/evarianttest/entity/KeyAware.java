package org.vm.evarianttest.entity;

/**
 * This interface defines the contract supporting a "Key" which uniquely identifies an entity in the system.
 *
 * @author vivekm
 * @since 1.0
 */
public interface KeyAware {
    /**
     * This method returns the Key object for the given KeyAware entity, which should be unique in the system and entity context.
     *
     * @return Key - An unique key for the given entity context
     */
    public Key key();
}