package org.vm.evarianttest.loader;

/**
 * This class represents the Exceptions that is specific to Data Loading operation.
 *
 * @author vivekm
 * @since 1.0
 */
public class DataLoaderException extends Exception {
    /**
     * Constructor with just message. Use this in case of validation exceptions.
     *
     * @param message - String
     */
    public DataLoaderException(String message){
        super(message);
    }

    /**
     * Constructor that can take message and Throwable exception, that is probably the root cause.
     *
     * @param message - String
     * @param cause - Throwable
     */
    public DataLoaderException(String message, Throwable cause){
        super(message, cause);
    }
}
