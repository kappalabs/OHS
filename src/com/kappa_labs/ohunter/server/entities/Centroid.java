
package com.kappa_labs.ohunter.server.entities;

/**
 * This class provides storage for temporary computations, when doing K-Means.
 */
public class Centroid extends Pixel {
    
    private int numPixels;
    private final Pixel sum;

    /**
     * Creates new Centroid object, the Pixel is initialized to zeros.
     */
    public Centroid() {
        super(0, 0, 0);
        sum = new Pixel(0, 0, 0);
    }
    
    /**
     * Adds another Pixel for the mean computation.
     * 
     * @param pixel The pixel, which should be added to compute the mean.
     */
    public void addPixel(Pixel pixel) {
        numPixels++;
        sum.add(pixel);
    }
    
    /**
     * Counts the mean of all pixels, which were added.
     * 
     * @return The mean pixel, counted from all added pixels, null if no pixel
     * is assigned to this centroid.
     */
    public Pixel getMean() {
        return (numPixels == 0) ? null : sum.divide(numPixels);
    }
}
