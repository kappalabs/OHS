
package com.kappa_labs.ohunter.server.utils;

/**
 * Interface providing action to simply add elements to an object.
 * 
 * @param <E> The type of elements to add by the addterator
 */
public interface Addable<E> {

    /**
     * Returns an addterator over elements of type E.
     * 
     * @return An addterator over elements of type E.
     */
    public Addterator<E> addterator();
    
}
