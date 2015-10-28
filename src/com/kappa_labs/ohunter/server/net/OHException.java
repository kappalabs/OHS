
package com.kappa_labs.ohunter.server.net;

/**
 * Exception, that can be thrown when communicating from client to this server.
 */
public class OHException extends Exception {
    
    /**
     * Specifies type of this exception.
     */
    public enum EXType {
        DUPLICATE_USER, INCORRECT_PASSWORD, INCORRECT_USER, INCORRECT_PASS_OR_NO_USER,
        DATABASE_ERROR, /* this error type means try it later again */
        OTHER
    }
    
    private EXType exType;

    /**
     * Creates new exception, message and type must be specified.
     * Message can be shown on clients side when no other option is available.
     * 
     * @param message Short info about this exception.
     * @param exType Specifies type of this exception.
     */
    public OHException(String message, EXType exType) {
        super(message);
        this.exType = exType;
    }

    /**
     * Gets the specific type of this exception.
     * 
     * @return The specific type of this exception.
     */
    public EXType getExType() {
        return exType;
    }
    
}
