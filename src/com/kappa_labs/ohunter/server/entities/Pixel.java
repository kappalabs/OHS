package com.kappa_labs.ohunter.server.entities;

/**
 * Class representing one pixel in image. The color model used here is HSV.
 * It also contains fields helpful for the computation of K-Means algorithm.
 */
public class Pixel {

    /**
     * Special value which should be assigned if the centroid for this pixel is
     * unknown.
     */
    public static final int UNKNOWN_CENTROID = -1;

    /**
     * Position of the pixel in the image.
     */
    public int x, y;
    /**
     * Elements of the HSV color model.
     */
    private float h, s, v;
    /**
     * For storing the distance to closest mean pixel.
     */
    public double distance = Double.MAX_VALUE;
    /**
     * Index of the closest mean pixel.
     */
    public int centroid = UNKNOWN_CENTROID;

    
    /**
     * Create new pixel with given HSV color elements, the position is
     * initialized to (0;0).
     *
     * @param h Hue in HSV model.
     * @param s Saturation in HSV model.
     * @param v Value in HSV model.
     */
    public Pixel(float h, float s, float v) {
        this.h = h;
        this.s = s;
        this.v = v;

        this.x = 0;
        this.y = 0;
    }

    /**
     * Create new pixel with given HSV color elements, the position is
     * initialized to given values.
     *
     * @param hsv HSV elements.
     * @param x The x coordinate of the pixel.
     * @param y The y coordinate of the pixel.
     */
    public Pixel(float[] hsv, int x, int y) {
        this.h = hsv[0];
        this.s = hsv[1];
        this.v = hsv[2];

        this.x = x;
        this.y = y;
    }

    /**
     * Create a deep copy of given Pixel.
     *
     * @param pixel The Pixel to be copied.
     */
    public Pixel(Pixel pixel) {
        this.h = pixel.h;
        this.s = pixel.s;
        this.v = pixel.v;

        this.x = pixel.x;
        this.y = pixel.y;

        this.centroid = pixel.centroid;
        this.distance = pixel.distance;
    }

    /**
     * Get i-th color element of this pixel. 0 is Hue, 1 is Saturation, 2 is
     * Value, otherwise return NaN.
     *
     * @param index Index of the color element to return.
     * @return The color element accordingly to given index.
     */
    public float get(int index) {
        switch (index) {
            case 0:
                return h;
            case 1:
                return s;
            case 2:
                return v;
            default:
                return Float.NaN;
        }
    }

    /**
     * Count distance to another given pixel. This distance is uded in K-Means.
     * Because areas in the picture should be coherent, the distance also takes
     * into account the distance between coordinates, not just the color
     * distance.
     *
     * @param second The pixel, to which the distance is counted to.
     * @return The distance measured by color and space difference.
     */
    public double distance(Pixel second) {
//        /* NOTE: pro HSV model */
//        return 0.00000001 + Math.sqrt(.0000005*Math.pow(second.x - this.x, 2) + .0000005*Math.pow(second.y - this.y, 2) +
//                .333333*Math.pow(second.h - this.h, 2) + .333333*Math.pow(second.s - this.s, 2) + .333333*Math.pow(second.v - this.v, 2));

        return Math.sqrt(Math.pow(second.h - this.h, 2)
                + Math.pow(second.s - this.s, 2) + Math.pow(second.v - this.v, 2));
    }

    /**
     * Performs an addition with given pixel (color and location). This pixel is
     * modified and returned.
     *
     * @param adder The pixel, whose elements should be added.
     * @return This modified pixel.
     */
    public Pixel add(Pixel adder) {
        h += adder.h;
        s += adder.s;
        v += adder.v;

        x += adder.x;
        y += adder.y;

        return this;
    }

    /**
     * Performs a division with given pixel (color and location) on every
     * element separately. This pixel is modified and returned.
     *
     * @param divider The pixel, whose elements will divide this pixel
     * @return This modified pixel.
     */
    public Pixel divide(double divider) {
        h /= divider;
        s /= divider;
        v /= divider;

        x /= divider;
        y /= divider;

        return this;
    }

    @Override
    public String toString() {
        return "Pixel: [h,s,v] = " + String.format("[%.2f, %.2f, %.2f]; ", h, s, v)
                + String.format("{dist}, (centr) = {%.2f}, (%d)", distance, centroid);
    }

}
