
package com.kappa_labs.ohunter.server.google_api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Class for storing information about place, retrieved by Google API.
 */
public class Place {

    /**
     * Default directory for storage of the photos.
     */
    public static final String PHOTOS_DIR = "places/";
    
    /**
     * Place unique identifier.
     */
    public String place_id;
    /**
     * Place location longitude.
     */
    public double longitude;
    /**
     * Place location latitude.
     */
    public double latitude;
    
    /**
     * Stores all photos assigned to this specific place.
     */
    public ArrayList<Photo> photos;

    
    /**
     * Creates new place object to store the information retrieved by Google API.
     */
    public Place() {
        photos = new ArrayList<>();
    }
    
    /**
     * Stores all photos in this object into local directory.
     * 
     * @param directory Directory, where the files should be stored
     *                  (must end with separator), default is used when null.
     */
    public void saveToFile(String directory) {
        try {
            if (directory == null) {
                directory = PHOTOS_DIR;
            }
            File fdir = new File(directory + place_id + File.separator);
            fdir.mkdirs();
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                File fphoto = new File(fdir.getPath() + File.separator + i + ".png");
                ImageIO.write(photo.image, "png", fphoto);
            }
        } catch (IOException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String toString() {
        return "Place: [" + longitude + ";" + latitude + "]; ID = " + place_id
                + "; #photos = " + ((photos == null) ? "null" : photos.size());
    }
    
}
