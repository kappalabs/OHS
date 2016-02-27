
package com.kappa_labs.ohunter.server.entities;

import java.util.ArrayList;


/**
 * Representation of one image segment, counted by Segmenter.
 */
public class Segment {
    
    /**
     * Number of elements used in the color model to represent the image.
     * (Should always be the same.)
     */
    public static final int MODEL_NUM_ELEMENTS = 3;
    
    /**
     * The position of centroid of this segment.
     */
    private int x, y;
    /**
     * The first color moment of this segment.
     */
    private float[] mean;
    /**
     * The second color moment of this segment.
     */
    private final float[] std_deviation;
    /**
     * The third color moment of this segment.
     */
    private final float[] skewness;
    /**
     * Number of pixels in this segment.
     */
    private int sum_pixels;
    /**
     * Defines the rectangle, that contains this whole segment.
     */
    private int top, left, bottom, right;
    
    
    private Segment() {
        /* Cannot be instantiated from outside of this class */
        mean = new float[MODEL_NUM_ELEMENTS];
        std_deviation = new float[MODEL_NUM_ELEMENTS];
        skewness = new float[MODEL_NUM_ELEMENTS];
        
        top = Integer.MAX_VALUE;
        bottom = Integer.MIN_VALUE;
        left = Integer.MAX_VALUE;
        right = Integer.MIN_VALUE;
    }
    
    /**
     * Count the segments from given MyImage, which did go through the segmentation
     * algorithm and the means, which is the output of the segmentation.
     * 
     * @param img MyImage, which did go through the segmentation.
     * @param means The output of the segmentation.
     * @return The segments with additional informations about themselves.
     */
    public static Segment[] makeSegments(MyImage img, Pixel[] means) {
        Segment[] segms = new Segment[means.length];
        
        for (int i = 0; i < means.length; i++) {
            if (means[i] == null) {
                continue;
            }
            segms[i] = new Segment();
            
            segms[i].x = means[i].x;
            segms[i].y = means[i].y;
            segms[i].mean = new float[]{means[i].get(0), means[i].get(1), means[i].get(2)};
        }
        
        /**
         * Count the other two moments, number of pixels in the segment and
         * the rectangle borders.
         */
        for (int i = 0; i < img.height; i++) {
            for (int j = 0; j < img.width; j++) {
                Pixel px = img.getPixel(j, i);
                Segment segm = segms[px.centroid];
                if (segm == null) {
                    continue;
                }
                for (int k = 0; k < MODEL_NUM_ELEMENTS; k++) {
                    segm.std_deviation[k] += Math.pow(px.get(k) - segm.mean[k], 2);
                    segm.skewness[k] += Math.pow(px.get(k) - segm.mean[k], 3);
                }
                /* Number of pixels in appropriate segment */
                segm.sum_pixels++;
                /* Adjust surounding rectangle */
                if (j < segm.left) {
                    segm.left = j;
                }
                if (j > segm.right) {
                    segm.right = j;
                }
                if (i < segm.top) {
                    segm.top = i;
                }
                if (i > segm.bottom) {
                    segm.bottom = i;
                }
            }
        }
        /* Count standard deviation and skewness */
        for (int i = 0; i < means.length; i++) {
            for (int j = 0; j < MODEL_NUM_ELEMENTS; j++) {
                if (segms[i] != null) {
                    segms[i].std_deviation[j] = (float) Math.sqrt(segms[i].std_deviation[j] / segms[i].sum_pixels);
                    segms[i].skewness[j] = (float) Math.cbrt(segms[i].skewness[j] / segms[i].sum_pixels);
                }
            }
        }
        
        /* Remove invalid segments */
        ArrayList<Segment> ss_ = new ArrayList<>();
        for (Segment s : segms) {
            if (s != null) {
                ss_.add(s);
            }
        }
        segms = ss_.toArray(new Segment[0]);
        
        return segms;
    }

    /**
     * Gets the X coordinate of the centroid.
     * 
     * @return The X coordinate of the centroid.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of the centroid.
     * 
     * @return The Y coordinate of the centroid.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the mean (the first color moment).
     * 
     * @return The mean (the first color moment).
     */
    public float[] getMean() {
        return mean;
    }

    /**
     * Gets the standard deviation (the second color moment).
     * 
     * @return The standard deviation (the second color moment).
     */
    public float[] getStdDeviation() {
        return std_deviation;
    }

    /**
     * Gets the skewness (the third color moment).
     * 
     * @return The skewness (the third color moment).
     */
    public float[] getSkewness() {
        return skewness;
    }

    /**
     * Gets the number of pixels in this segment.
     * 
     * @return The number of pixels in this segment.
     */
    public int getSumPixels() {
        return sum_pixels;
    }

    /**
     * Gets the top border of the surrounding rectangle.
     * @return The top border of the surrounding rectangle.
     */
    public int getTop() {
        return top;
    }

    /**
     * Gets the left border of the surrounding rectangle.
     * @return The left border of the surrounding rectangle.
     */
    public int getLeft() {
        return left;
    }

    /**
     * Gets the bottom border of the surrounding rectangle.
     * @return The bottom border of the surrounding rectangle.
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * Gets the right border of the surrounding rectangle.
     * @return The right border of the surrounding rectangle.
     */
    public int getRight() {
        return right;
    }
    
}
