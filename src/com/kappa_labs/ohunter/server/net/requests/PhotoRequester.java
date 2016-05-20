package com.kappa_labs.ohunter.server.net.requests;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.PhotoRequest;
import com.kappa_labs.ohunter.server.google_api.PlacesCommunicator;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the FillPlacesRequest from the OHL.
 */
public class PhotoRequester extends PhotoRequest {
    
    private final SettingsManager settingsManager = SettingsManager.getInstance();

    
    public PhotoRequester(PhotoRequest request) {
        super(request);
    }
    
    @Override
    public Response execute() throws OHException {
        /* Parallel download of the Photos */
        Photo[] photos = new Photo[photoReferences.length];
        ExecutorService executor = Executors.newFixedThreadPool(settingsManager.getPhotoPoolThreadsNumber());
        for (int i = 0; i < photos.length; i++) {
            photos[i] = new Photo();
            photos[i].reference = photoReferences[i];
            executor.submit(new PhotoFiller(photos[i], width, height));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(settingsManager.getPhotoPoolMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchRequester.class.getName()).log(Level.WARNING, null, ex);
            throw new OHException("Server too busy now!", OHException.EXType.SERVER_OCCUPIED);
        }

        /* Store the data in Response object */
        Response response = new Response(player);
        response.photos = photos;

        return response;
    }

    
    /**
     * Worker class to retrieve one photo.
     */
    private class PhotoFiller implements Callable<Void> {
        
        private final Photo photo;
        private final int width, height;

        /**
         * Creates a new worker to retrieve one photo.
         * 
         * @param photo The photo object that will contain the retrieved photo.
         * @param width Maximum width of the photo.
         * @param height Maximum height of the photo.
         */
        public PhotoFiller(Photo photo, int width, int height) {
            this.photo = photo;
            this.width = width;
            this.height = height;
        }

        @Override
        public Void call() throws Exception {
            /* Download the image and store it into the photo */
            PlacesCommunicator.photoRequest(photo, width, height);
            return null;
        }
    
    }
    
}
