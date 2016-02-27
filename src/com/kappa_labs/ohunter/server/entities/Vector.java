
package com.kappa_labs.ohunter.server.entities;

import com.kappa_labs.ohunter.server.utils.Addable;
import com.kappa_labs.ohunter.server.utils.Addterator;
import java.util.Locale;

/**
 * Class for representation of a vector, which specifies compressed information
 * about one segment in the image.
 */
public class Vector implements Addable<Float> {
    
    private final float[] values;
    public int dimension;

    
    /**
     * Create a new vector, with fixed dimension, initialize its values to zero.
     * 
     * @param dimension The dimension of this vector.
     */
    public Vector(int dimension) {
        this.dimension = dimension;
        
        values = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            values[i] = 0;
        }
    }
    
    /**
     * Set value of specified element.
     * 
     * @param indx Index of the element to modify.
     * @param value The value, to which the element should be set.
     */
    public void set(int indx, float value) {
        values[indx] = value;
    }
    
    /**
     * Gets the requested element value.
     * 
     * @param indx The index of requested element.
     * @return The value of requested element.
     */
    public float get(int indx) {
        return values[indx];
    }
    
    /**
     * Uses Manhatan's metrics to compute distance between this and given vector.
     * The value is then normalized, maximum value that can be returned is 1,
     * minimum is 0.
     * 
     * @param vectTo Second vector to which the distance will be measured.
     * @return Normalized L1 distance between this and given vector.
     */
    public float distance(Vector vectTo) {
        assert (vectTo.dimension == dimension) : "Incompatible dimension ("+vectTo.dimension+")!";
        float sum = 0;
        for (int i = 0; i < dimension; i++) {
            sum += Math.abs(get(i) - vectTo.get(i));
        }
        
        return sum;
    }
    
    /**
     * Simple check if the value is out of the [0;1] range.
     * 
     * @return True, if the value is out of the [0;1] range, false otherwise.
     */
    public boolean isOutOfRange() {
        for (float value : values) {
            if (value > 1.f || value < 0.f) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Addterator<Float> addterator() {
        return new Addterator<Float>() {
            
            private int indx;

            @Override
            public void add(Float element) {
                if (isFull()) {
                    throw new ArrayIndexOutOfBoundsException("You can call add() only dimension-times");
                }
                set(indx++, element);
            }

            @Override
            public boolean isFull() {
                return indx >= dimension;
            }
        };
    }

    @Override
    public String toString() {
        String ret = "Vector: ["+dimension+"] (";
        for (int i = 0; i < values.length; i++) {
            ret += String.format(Locale.ENGLISH, "%d:%.02f", i+1, values[i]);
            if (i != values.length - 1) {
                 ret += "; ";
            }
        }
        return ret + ")";
    }
    
}
