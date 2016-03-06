
package com.kappa_labs.ohunter.server.utils;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import com.kappa_labs.ohunter.server.google_api.PlacesGetter;
import java.util.ArrayList;


/**
 * Worker class for filling up given Place object with information and photos.
 */
public class PlaceFiller implements Runnable {

    private final Place mPlace;
    private final ArrayList<Place> mPlaces;
    private final int width, height;


    /**
     * Creates a new Place filler to fill the detail information and photos
     * into given place. The result Place is added into the List only when no error
     * occures and at least one photo is available for that place.
     * 
     * @param place The place whose details will be retrieved.
     * @param places The List, where the valid result should be placed.
     * @param width The maximum width of photos for given place.
     * @param height The maximum height of photos for given place.
     */
    public PlaceFiller(Place place, ArrayList<Place> places, int width, int height) {
        this.mPlace = place;
        this.mPlaces = places;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        ArrayList<Photo> photos = PlacesGetter.details(mPlace);
        if (photos == null) {
            return;
        }
        mPlace.photos = photos;
        photos.stream().forEach((photo) -> {
            PlacesGetter.photoRequest(photo, width, height);
            if (Analyzer.isNight(photo)) {
                photo.daytime = Photo.DAYTIME.NIGHT;
            }
        });

        mPlaces.add(mPlace);
    }

}
