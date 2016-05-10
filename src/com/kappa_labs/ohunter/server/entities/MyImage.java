package com.kappa_labs.ohunter.server.entities;

import java.util.Iterator;

/**
 * Class stores image, so that it's more suitable for the segmentation process.
 * Provides the ability to iterate through the whole image pixel by pixel.
 */
public class MyImage implements Iterable<Pixel> {

    /* Pattern: [column|x|j][row|y|i] */
    private Pixel[][] image;
    private Pixel[][] _image;
    /**
     * Width of this image.
     */
    public int width;
    /**
     * Height of this image.
     */
    public int height;

    
    /**
     * Creates a new MyImage object from Pixel array. The first dimension of the
     * array must be width, the second height.
     *
     * @param image Image specified by Pixel array.
     * @param width The size of the first dimension of the pixel array.
     * @param height The size of the second dimension of the pixel array.
     */
    public MyImage(Pixel[][] image, int width, int height) {
        this.image = image;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a new deep copy of given MyImage.
     *
     * @param myImage The MyImage to make deep copy of.
     */
    public MyImage(MyImage myImage) {
        this.width = myImage.width;
        this.height = myImage.height;
        if (myImage.image != null) {
            this.image = new Pixel[myImage.width][myImage.height];
            for (int i = 0; i < this.width; i++) {
                for (int j = 0; j < this.height; j++) {
                    this.image[i][j] = new Pixel(myImage.image[i][j]);
                }
            }
        }
        if (myImage._image != null) {
            this.image = new Pixel[myImage.width][myImage.height];
            for (int i = 0; i < this.width; i++) {
                for (int j = 0; j < this.height; j++) {
                    this._image[i][j] = new Pixel(myImage._image[i][j]);
                }
            }
        }
    }

    /**
     * Gets a pixel from specific location. The location is not being checked.
     *
     * @param x The x coordinate of desired pixel.
     * @param y The y coordinate of desired pixel.
     * @return The pixel from required location.
     */
    public Pixel getPixel(int x, int y) {
        assert (x >= 0 && x < width && y >= 0 && y < height) : "Location is outside MyImage!";
        return image[x][y];
    }

    /**
     * Select a row, then iterate through its columns. Goes through each pixel
     * in the image.
     *
     * @return Iterator, which can go through the whole image.
     */
    @Override
    public Iterator<Pixel> iterator() {
        return new Iterator<Pixel>() {

            int i = 0, j = 0;

            @Override
            public boolean hasNext() {
                tryNextRow();
                return i < height && image[j][i] != null;
            }

            @Override
            public Pixel next() {
                tryNextRow();
                return image[j++][i];
            }

            private void tryNextRow() {
                if (j == width) {
                    j = 0;
                    i++;
                }
            }
        };
    }

}
