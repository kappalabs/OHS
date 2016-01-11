
package com.kappa_labs.ohunter.server.entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 */
public class SImage extends com.kappa_labs.ohunter.lib.entities.SImage {

    /**
     * Creates an empty SImage, with uninitialized fields.
     */
    public SImage() { }
    
    /**
     * Create new SImage from BufferedImage.
     * 
     * @param bimage BufferedImage to be converted to new SImage.
     */
    public SImage(BufferedImage bimage) {
        this.image = toBytes(bimage);
        this.width = bimage.getWidth();
        this.height = bimage.getHeight();
    }
    
    /**
     * Convert given BufferedImage to byte array with 3-byte BGR type.
     * 
     * @param bi BufferedImage to be converted.
     * @return Byte array representation of the BufferedImage image data.
     */
    public static byte[] toBytes(BufferedImage bi) {
        bi = convertToRGB(bi);
        return ((DataBufferByte) bi.getData().getDataBuffer()).getData();
    }
    
    
    public BufferedImage toBufferedImage() {
        /* TODO: cash */
        ByteArrayInputStream bais = new ByteArrayInputStream(image);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Convert given BufferedImage to 3BYTE BRG type.
     * 
     * @param image Image to be converted.
     * @return The converted BufferedImage into 3BYTE BRG type.
     */
    public static BufferedImage convertToRGB(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        
        return newImage;
    }
    
    /**
     * Sets the internal image to given BufferedImage image.
     * 
     * @param bimage The BufferedImage image, which will be inserted.
     */
    public void setImage(BufferedImage bimage) {
        this.image = toBytes(bimage);
        this.width = bimage.getWidth();
        this.height = bimage.getHeight();
    }
    
}
