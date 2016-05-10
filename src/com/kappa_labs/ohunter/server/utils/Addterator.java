package com.kappa_labs.ohunter.server.utils;

/**
 * An addterator over a collection.
 *
 * @param <E> The type of elements to add by this addterator.
 */
public interface Addterator<E> {

    /**
     * Adds an element to the next free position.
     *
     * @param element Element to be added.
     */
    void add(E element);

    /**
     * Returns true, if the addterator can NOT add next element.
     *
     * @return True, if the addterator can NOT add next element.
     */
    boolean isFull();

}
