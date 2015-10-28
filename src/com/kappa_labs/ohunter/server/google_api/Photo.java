
package com.kappa_labs.ohunter.server.google_api;

import java.awt.image.BufferedImage;

/**
 * Class for storing the information about photo retrieved by Google API.
 */
public class Photo {
    
    /**
     * Reference retrieved from Google API.
     */
    public String reference;
    public BufferedImage image;
    public BufferedImage _image;
    
    /**
     * Return the widht of assigned image or 0 if not initialized yet.
     * @return The widht of assigned image or 0 if not initialized yet.
     */
    public int getWidth() {
        return (image == null) ? 0 : image.getWidth();
    }
    
    /**
     * Return the height of assigned image or 0 if not initialized yet.
     * @return The height of assigned image or 0 if not initialized yet.
     */
    public int getHeight() {
        return (image == null) ? 0 : image.getHeight();
    }

    @Override
    public String toString() {
        return "Photo: " + getWidth() + "x" + getHeight() + "; ref = " + reference;
    }
    
}
