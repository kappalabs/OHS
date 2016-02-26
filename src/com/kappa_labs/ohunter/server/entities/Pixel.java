
package com.kappa_labs.ohunter.server.entities;

import com.kappa_labs.ohunter.server.utils.CIELab;

/**
 * Class representing one pixel in image. The color model used here is CIELab.
 * It also contains fields helpful for the computation of K-Means algorithm.
 */
public class Pixel {
    
    public static final int UNKNOWN_CENTROID = -1;

    /**
     * Position of the pixel in the image.
     */
    public int x, y;
    /**
     * Elements of the CIELab color model.
     */
    private float l, a, b;
    /**
     * For storing the distance to closest mean pixel.
     */
    public double distance = Double.MAX_VALUE;
    /**
     * Index of the closest mean pixel.
     */
    public int centroid = UNKNOWN_CENTROID;
    
    
    /**
     * Create new pixel with given CIELab color elements, the position is 
     * initialized to (0;0).
     * 
     * @param l Lightness in CIELab model.
     * @param a Red-green in CIELab model.
     * @param b Yellow-blue in CIELab model.
     */
    public Pixel(float l, float a, float b) {
        this.l = l;
        this.a = a;
        this.b = b;
        
        this.x = 0;
        this.y = 0;
    }
    
    /**
     * Create new pixel with given CIELab color elements, the position is 
     * initialized to given values.
     * 
     * @param lab CIELab elements.
     * @param x The x coordinate of the pixel.
     * @param y The y coordinate of the pixel.
     */
    public Pixel(float[] lab, int x, int y) {
        this.l = lab[0];
        this.a = lab[1];
        this.b = lab[2];
        
        this.x = x;
        this.y = y;
    }
    
    /**
     * Transform color of this pixel to RGB integer.
     * 
     * @return The color of this pixel in RGB model.
     */
    public int getRGBValue() {
        float[] rgb = CIELab.getInstance().toRGB(new float[]{l, a, b});
        
        int A = 0xFF000000;
        int R = Math.round(255 * rgb[0]);
        int G = Math.round(255 * rgb[1]);
        int B = Math.round(255 * rgb[2]);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return A | R | G | B;
    }
    
    /**
     * Get i-th color element of this pixel.
     * 0 is L, 1 is A, 2 is B, otherwise return NaN.
     * 
     * @param index Index of the color element to return.
     * @return The color element accordingly to given index.
     */
    public float get(int index) {
        switch (index) {
            case 0:
                return l;
            case 1:
                return a;
            case 2:
                return b;
            default:
                return Float.NaN;
        }
    }
    
    /**
     * Count distance to another given pixel. This distance is uded in K-Means.
     * Because areas in the picture should be coherent, the distance also takes
     * into account the distance between coordinates, not just the color distance.
     * 
     * @param second The pixel, to which the distance is counted to.
     * @return The distance measured by color and space difference.
     */
    public double distance(Pixel second) {
//        return Math.sqrt(.02*Math.pow(second.x - this.x, 2) + .02*Math.pow(second.y - this.y, 2) +
//                .32*Math.pow(second.l - this.l, 2) + .32*Math.pow(second.a - this.a, 2) + .32*Math.pow(second.b - this.b, 2));
        
        if (this.x == second.x && this.y == second.y) {
            return 0;
        }
        /* NOTE: pro HSV model */
        return 0.0000001 + Math.sqrt(.000005*Math.pow(second.x - this.x, 2) + .000005*Math.pow(second.y - this.y, 2) +
                .33333*Math.pow(second.l - this.l, 2) + .33333*Math.pow(second.a - this.a, 2) + .33333*Math.pow(second.b - this.b, 2));
//        
        /* NOTE: vyuziti vyhody CIELab modelu - pouziti pouze dvou dimenzi pro barvy */
//        return Math.sqrt(.1*Math.pow(second.x - this.x, 2) + .1*Math.pow(second.y - this.y, 2) +
//                .4*Math.pow(second.l - this.l, 2) + .4*Math.pow(second.a - this.a, 2));
        
        
//        return Math.sqrt(.1*Math.pow(second.x - this.x, 2) + .1*Math.pow(second.y - this.y, 2))
//                * Math.sqrt(.4*Math.pow(second.l - this.l, 2) + .4*Math.pow(second.a - this.a, 2));
//        return Math.sqrt(Math.pow(second.l - this.l, 2) + Math.pow(second.a - this.a, 2));
    }
    
    /**
     * Performs an addition with given pixel (color and location). This pixel
     * is modified and returned.
     * 
     * @param adder The pixel, whose elements should be added.
     * @return This modified pixel.
     */
    public Pixel add(Pixel adder) {
        l += adder.l;
        a += adder.a;
        b += adder.b;
        
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
        l /= divider;
        a /= divider;
        b /= divider;
        
        x /= divider;
        y /= divider;
        
        return this;
    }

    @Override
    public String toString() {
        return "Pixel: a,b = "+a+","+b+"; dist,centr = "+distance+","+centroid;
    }
    
}
