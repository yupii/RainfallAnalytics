package org.vm.evarianttest.entity;

/**
 * This class represents a Key to an entity that the system uses. It is merely an object that can uniquely
 * identify that entity in the given context. We could add a key type to make it context aware, but not now. Also keys
 * should support equals rule, if two keys in given context are same, then the entities are same.
 *
 * @author vivekm
 * @since 1.0
 */
public class Key implements Comparable{
    /** Immutable key object */
    private Object key;

    /**
     * Constructor for the Key.
     *
     * @param key
     */
    public Key(final Object key){
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key1 = (Key) o;

        return key.equals(key1.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Key{");
        sb.append("key=").append(String.valueOf(key));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return ((String)key).compareTo((String)((Key)o).key);
    }
}
