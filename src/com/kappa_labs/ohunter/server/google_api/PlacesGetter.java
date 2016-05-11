package com.kappa_labs.ohunter.server.google_api;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.server.utils.SettingsManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class providing connection with Google API.
 */
public class PlacesGetter {
    
    private static final SettingsManager settingsManager = SettingsManager.getInstance();

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_RADAR_SEARCH = "/radarsearch";

    /* Recomended way of retrieving result data */
    private static final String OUT_JSON = "/json";
    private static final String OUT_PHOTO = "/photo";

    
    /**
     * Performs request on Google API Radar Search. Returns up to 200 result
     * places in List.
     *
     * @param keyword Optional, can specify the places exactly (by their name).
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @param radius The radius of search are around given location in meters.
     * @param types Types of objects as GAPI specifies.
     * @return List of all places, that were returned by the request.
     */
    public static List<Place> radarSearch(double latitude, double longitude, int radius, String keyword, String types) {
        List<Place> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_RADAR_SEARCH);
            sb.append(OUT_JSON);
            sb.append("?key=").append(settingsManager.getGoogleAPIKey());
            sb.append("&location=").append(String.valueOf(latitude)).
                    append(",").append(String.valueOf(longitude));
            sb.append("&radius=").append(String.valueOf(radius));
            sb.append("&keyword=").append(URLEncoder.encode(keyword, "utf8"));
            sb.append("&types=").append(URLEncoder.encode(types, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            /* Load the results into a StringBuilder */
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error processing Places API URL", ex);
            return resultList;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error connecting to Places API", ex);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            /* Create a JSON object hierarchy from the results */
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) ((JSONObject) parser.parse(jsonResults.toString())).get("results");

            /* Extract the Place descriptions from the results */
            resultList = new ArrayList<>(array.size());
            for (Object element : array) {
                Place place = new Place();
                place.setID((String) ((JSONObject) element).get("place_id"));
                JSONObject location = (JSONObject) ((JSONObject) ((JSONObject) element).get("geometry")).get("location");
                place.latitude = (Double) location.get("lat");
                place.longitude = (Double) location.get("lng");
                resultList.add(place);
            }
        } catch (ParseException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error while parsing JSON results", ex);
        }

        return resultList;
    }

    /**
     * Performs request on Google API Place Details. Has acess to aditional
     * information about place, which is specified by place_id. References to
     * photos of this place will be returned in List. Aditional details will be
     * added to the given Place object.
     *
     * @param place Place object with place_id String, that was returned by
     * Radar Search to specify a place.
     * @return List of photos. They will contain only references, not the image
     * itself yet.
     */
    public static List<Photo> details(Place place) {
        List<Photo> photoList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_DETAILS);
            sb.append(OUT_JSON);
            sb.append("?key=").append(settingsManager.getGoogleAPIKey());
            sb.append("&placeid=").append(URLEncoder.encode(place.getID(), "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            /* Load the results into a StringBuilder */
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error processing Places API URL", ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error connecting to Places API", ex);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            /* Create a JSON object hierarchy from the results */
            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) ((JSONObject) parser.parse(jsonResults.toString())).get("result");

            /* TODO: filter podle jmena */
            String name = (String) ((JSONObject) result).get("name");
            if (filterByName(name)) {
                return null;
            }

            /* Add basic information about the location if necessary */
            if (place.latitude == 0 && place.longitude == 0) {
                JSONObject location = (JSONObject) ((JSONObject) result.get("geometry")).get("location");
                place.latitude = (Double) location.get("lat");
                place.longitude = (Double) location.get("lng");
            }
            /* Add aditional information about the place */
            place.putGField("name", name);
            place.putGField("formatted_address", (String) ((JSONObject) result).get("formatted_address"));
            place.putGField("url", (String) ((JSONObject) result).get("url"));
            place.putGField("website", (String) ((JSONObject) result).get("website"));
            place.putGField("icon", (String) ((JSONObject) result).get("icon"));

            /* Extract the photo references for this place from the results */
            JSONArray photos = (JSONArray) result.get("photos");
            if (photos == null) {
                return null;
            }
            photoList = new ArrayList<>(photos.size());
            Photo photo;
            for (Object photoObj : photos) {
                photo = new Photo();
                photo.reference = (String) ((JSONObject) photoObj).get("photo_reference");
                photoList.add(photo);
            }
        } catch (ParseException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error while parsing JSON results", ex);
        }

        return photoList;
    }

    /**
     * This method can contain hardcoded strings, that will be filtered from the
     * results of radar search.
     *
     * @param name Name of the place given by Place Details.
     * @return True, if the name is inappropriate, false otherwise.
     */
    private static boolean filterByName(String name) {
        return false;
    }

    /**
     * Request on Google API, which gets the actual image. The image is set into
     * given Photo object and returned by this function. The maximum sizes are
     * 1600x1600.
     *
     * @param photo The Photo object, where the image should be added.
     * @return The same Photo object with associated image.
     */
    public static Photo photoRequest(Photo photo) {
        return photoRequest(photo, 1600, 1600);
    }

    /**
     * Request on Google API, which gets the actual image. The image is set into
     * given Photo object and returned by this function.
     *
     * If the image is smaller than the values specified, the original image
     * will be returned. If the image is larger in either dimension, it will be
     * scaled to match the smaller of the two dimensions, restricted to its
     * original aspect ratio. Both the maxheight and maxwidth properties accept
     * an integer between 1 and 1600.
     *
     * @param photo The Photo object, where the image should be added.
     * @param maxWidth As described above.
     * @param maxHeight As described above.
     * @return The same Photo object with associated image.
     */
    public static Photo photoRequest(Photo photo, int maxWidth, int maxHeight) {
        HttpURLConnection conn = null;

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(OUT_PHOTO);
            sb.append("?key=").append(settingsManager.getGoogleAPIKey());
            sb.append("&photoreference=").append(URLEncoder.encode(photo.reference, "utf8"));
            sb.append("&maxwidth=").append(String.valueOf(maxWidth));
            sb.append("&maxheight=").append(String.valueOf(maxHeight));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            photo.sImage = new SImage();
            try {
                ((SImage) photo.sImage).setImage(ImageIO.read(url));
            } catch (NullPointerException ex) {
                /* Rare exception - Java bug https://bugs.openjdk.java.net/browse/JDK-8058973 */
                Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                        "Error while getting the Photo", ex);
                photo.sImage = null;
                return null;
            }
            if (photo.sImage == null) {
                return null;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error processing Places API URL", ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PlacesGetter.class.getName()).log(Level.WARNING,
                    "Error connecting to Places API", ex);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return photo;
    }

}
