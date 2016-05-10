package com.kappa_labs.ohunter.server.entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Server side extension of the basic SImage from library. Supports
 * BufferedImage operations.
 */
public class SImage extends com.kappa_labs.ohunter.lib.entities.SImage {

    private boolean invalidated = true;
    private BufferedImage _image;

    
    /**
     * Creates an empty SImage, with uninitialized fields.
     */
    public SImage() {
    }

    /**
     * Create new SImage from BufferedImage.
     *
     * @param bimage BufferedImage to be converted to new SImage.
     */
    public SImage(BufferedImage bimage) {
        this.bytes = toBytes(bimage);
        this.width = bimage.getWidth();
        this.height = bimage.getHeight();
    }

    /**
     * Create new SImage from library version of SImage.
     *
     * @param simage The library version of SImage.
     */
    public SImage(com.kappa_labs.ohunter.lib.entities.SImage simage) {
        super(simage);
    }

    /**
     * Convert given BufferedImage to byte array in JPG format.
     *
     * @param bi BufferedImage to be converted.
     * @return Byte array representation of the BufferedImage image data.
     */
    public static byte[] toBytes(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "jpg", baos);
        } catch (IOException ex) {
            Logger.getLogger(SImage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return baos.toByteArray();
    }

    /**
     * Converts the internal byte array to BufferedImage object. NOTE: The
     * object is cashed after the first call.
     *
     * @return The BufferedImage converted from internal image data.
     */
    public BufferedImage toBufferedImage() {
        if (invalidated || (_image == null && bytes != null)) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            try {
                _image = ImageIO.read(bais);
                invalidated = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return _image;
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
        this.bytes = toBytes(bimage);
        this.width = bimage.getWidth();
        this.height = bimage.getHeight();

        invalidated = true;
    }

}
