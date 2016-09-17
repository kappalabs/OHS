package com.kappa_labs.ohunter.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages loading all the settings for the whole project.
 */
public class SettingsManager {

    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());

    private static final Map<String, Object> PROPERTIES = new HashMap<>();
    private static final Map<String, Property<?>> DEFAULT_PROPERTIES = new LinkedHashMap<>();

    /**
     * Path to the configuration file.
     */
    private static final String SETTINGS_FILENAME = "config.txt";

    /* Values for Server */
    private static final String SERVER_ADDRESS_IP_KEY = "server.address.ip";
    private static final String SERVER_PORT_KEY = "server.port";
    private static final String SERVER_CLIENT_POOL_SIZE_KEY = "server.client_pool_size";

    /* Values for Database */
    private static final String DATABASE_NAME_KEY = "database.name";
    private static final String DATABASE_USER_KEY = "database.user";
    private static final String DATABASE_PASSWORD_KEY = "database.password";
    private static final String DATABASE_LOCATION_KEY = "database.location";

    /* Values for the game */
    private static final String INITIAL_SCORE_KEY = "game.initial_score";
    private static final String GOOGLE_API_KEY = "game.google_api_key";

    /* Values for Analyzer */
    private static final String RANDOM_PHOTO_SAMPLES_KEY = "analyzer.daytime.random_photo_samples";
    private static final String NIGHT_THRESHOLD_KEY = "analyzer.daytime.night_threshold";
    private static final String OPTIMAL_PHOTO_WIDTH_KEY = "analyzer.similarity.optimal_photo_width";
    private static final String OPTIMAL_PHOTO_HEIGHT_KEY = "analyzer.similarity.optimal_photo_height";

    /* Values for Segmenter */
    private static final String KMEANS_NUM_SEGMENTS_KEY = "segmenter.kmeans_num_segments";
    private static final String KMEANS_MAX_REPEATS_KEY = "segmenter.kmeans_max_repeats";
    private static final String KMEANS_INIT_REPEATS_KEY = "segmenter.kmeans_init_repeats";
    private static final String KMEANS_SAVE_FOR_DEBUG_KEY = "segmenter.kmeans_save_for_debug";
    private static final String SIMILARITY_NUM_REPEATS = "segmenter.similarity_num_repeats";

    /* Values for PhotoRequester */
    private static final String PHOTO_POOL_MAX_WAIT_TIME_KEY = "photo_requester.photo_pool_max_wait_time";
    private static final String PHOTO_POOL_SIZE_KEY = "photo_requester.photo_pool_size";

    /* Values for FillPlacesRequester */
    private static final String FILL_POOL_MAX_WAIT_TIME_KEY = "fill_requester.fill_pool_max_wait_time";
    private static final String FILL_POOL_FILLER_SIZE_KEY = "fill_requester.fill_pool_filler_size";
    private static final String FILL_POOL_PHOTO_SIZE_KEY = "fill_requester.fill_pool_photo_size";

    private static SettingsManager settingsManager;

    /**
     * Initializes the static fields in this class, loads the settings and
     * creates the config file if does not exist.
     */
    static {
        /* Values for Server */
        DEFAULT_PROPERTIES.put(SERVER_ADDRESS_IP_KEY, new Property("",
                "The IP adress where the server should run. Empty will cause console prompt."));
        DEFAULT_PROPERTIES.put(SERVER_PORT_KEY, new Property(4242,
                "The port which the server should use."));
        DEFAULT_PROPERTIES.put(SERVER_CLIENT_POOL_SIZE_KEY, new Property(8,
                "The size of thread pool for clients."));

        /* Values for Database */
        DEFAULT_PROPERTIES.put(DATABASE_NAME_KEY, new Property("oHunterDB",
                "The name of the database."));
        DEFAULT_PROPERTIES.put(DATABASE_USER_KEY, new Property("",
                "The user for the database."));
        DEFAULT_PROPERTIES.put(DATABASE_PASSWORD_KEY, new Property("",
                "The password for the database."));
        String userHomeDir = System.getProperty("user.home", ".");
        DEFAULT_PROPERTIES.put(DATABASE_LOCATION_KEY, new Property(userHomeDir
                + File.separator + "." + DEFAULT_PROPERTIES.get(DATABASE_NAME_KEY).getValue(),
                "The path to the location of the database."));

        /* Values for the game */
        DEFAULT_PROPERTIES.put(INITIAL_SCORE_KEY, new Property(100,
                "The initial score for a new player."));

        DEFAULT_PROPERTIES.put(GOOGLE_API_KEY, new Property("",
                "<klic>",
                "The Google API key for Google Places requests."));

        /* Values for Analyzer */
        DEFAULT_PROPERTIES.put(RANDOM_PHOTO_SAMPLES_KEY, new Property(128,
                "The number of random samples to determine the daytime."));
        DEFAULT_PROPERTIES.put(NIGHT_THRESHOLD_KEY, new Property(75,
                "The intensity treshold for determining night photos."));
        DEFAULT_PROPERTIES.put(OPTIMAL_PHOTO_WIDTH_KEY, new Property(256,
                "The optimal width of photo for measuring similarity."));
        DEFAULT_PROPERTIES.put(OPTIMAL_PHOTO_HEIGHT_KEY, new Property(256,
                "The optimal height of photo for measuring similarity."));

        /* Values for Segmenter */
        DEFAULT_PROPERTIES.put(KMEANS_NUM_SEGMENTS_KEY, new Property(64,
                "The maximum number of repeats for the K-Means algorithm itself."));
        DEFAULT_PROPERTIES.put(KMEANS_MAX_REPEATS_KEY, new Property(16,
                "The maximum number of repeats for the K-Means algorithm itself."));
        DEFAULT_PROPERTIES.put(KMEANS_INIT_REPEATS_KEY, new Property(32,
                "The number of repeats when trying to find the best initial pixels."));
        DEFAULT_PROPERTIES.put(KMEANS_SAVE_FOR_DEBUG_KEY, new Property(1,
                "1 if the segmenter should save debug images, 0 otherwise"));
        DEFAULT_PROPERTIES.put(SIMILARITY_NUM_REPEATS, new Property(5,
                "Number of repeats of the similarity measure algorithm"));

        /* Values for PhotoRequester */
        DEFAULT_PROPERTIES.put(PHOTO_POOL_MAX_WAIT_TIME_KEY, new Property(1,
                "The number of minutes to wait for thread termination."));
        DEFAULT_PROPERTIES.put(PHOTO_POOL_SIZE_KEY, new Property(10,
                "The number of threads allowed to retrieve the photos."));

        /* Values for FillPlacesRequester */
        DEFAULT_PROPERTIES.put(FILL_POOL_MAX_WAIT_TIME_KEY, new Property(1,
                "The number of minutes to wait for threads termination."));
        DEFAULT_PROPERTIES.put(FILL_POOL_FILLER_SIZE_KEY, new Property(32,
                "The number of threads allowed for PlaceFiller thread pool."));
        DEFAULT_PROPERTIES.put(FILL_POOL_PHOTO_SIZE_KEY, new Property(10,
                "The number of threads allowed to retrieve photos for each place."));

        readSettings();
    }

    
    private SettingsManager() {
        /* Non-instantiable class */
    }

    /**
     * Get instance of this class.
     *
     * @return The instance of this class.
     */
    public static SettingsManager getInstance() {
        if (settingsManager == null) {
            settingsManager = new SettingsManager();
        }
        return settingsManager;
    }

    /**
     * Reloads all the settings from the config file, creates it if it does not
     * exist
     */
    public void reloadSettings() {
        readSettings();
    }

    /**
     * Saves the default configuration into the config file.
     */
    private static void createDefaultConfig() {
        DEFAULT_PROPERTIES.entrySet().stream().forEach((entry) -> {
            addProperty(entry.getKey(), entry.getValue());
        });
    }

    /**
     * Reads the configuration into the memory from config file, creates the
     * config file if does not exist.
     */
    private static void readSettings() {
        BufferedReader bReader = null;
        try {
            File settingsFile = new File(SETTINGS_FILENAME);
            if (settingsFile.createNewFile()) {
                createDefaultConfig();
                LOGGER.log(Level.FINE, "Created new configuration file in {0}", settingsFile.getAbsolutePath());
            }
            bReader = new BufferedReader(new FileReader(settingsFile));
            String line;
            while ((line = bReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] tokens = line.split("=");
                if (tokens.length != 2) {
                    LOGGER.log(Level.SEVERE, "Wrong configuration format at line: {0}", line);
                    continue;
                }
                String key = tokens[0].trim();
                String value = tokens[1].trim();
                try {
                    if (value.startsWith("\"")) {
                        PROPERTIES.put(key, value.substring(1, value.length() - 1));
                    } else {
                        PROPERTIES.put(key, Integer.valueOf(value));
                    }
                } catch (NumberFormatException _ex) {
                    LOGGER.log(Level.SEVERE, "Wrong configuration format at line: {0}", line);
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Writes given property to the config file.
     *
     * @param key Key of the property.
     * @param value Value of the property.
     */
    private static void addProperty(String key, Property property) {
        PrintWriter printWriter = null;
        try {
            File settingsFile = new File(SETTINGS_FILENAME);
            printWriter = new PrintWriter(new FileOutputStream(settingsFile, true));
            printWriter.println("# " + property.getDescription());
            if (property.getPublicValue() instanceof String) {
                printWriter.println(key + " = \"" + property.getPublicValue() + "\"");
            } else {
                printWriter.println(key + " = " + property.getPublicValue());
            }
            printWriter.println();
            printWriter.flush();
            LOGGER.log(Level.FINER, "Property <{0}; {1}> was written to config.", new Object[]{key, property.getPublicValue()});
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    /**
     * Gets the integer property from the hash table. If the value is not
     * present, it saves and returns the default value.
     *
     * @param key The key for the requested property value.
     * @return The value for given property key.
     */
    private int getIntegerProperty(String key) {
        Integer value = (Integer) PROPERTIES.get(key);
        boolean isExport = Objects.equals(value, (String) DEFAULT_PROPERTIES.get(key).getExportValue());
        if (value == null || isExport) {
            Property prop = DEFAULT_PROPERTIES.get(key);
            value = (Integer) prop.getValue();
            if (!isExport) {
                addProperty(key, prop);
            }
        }
        return value;
    }

    /**
     * Gets the string property from the hash table. If the value is not
     * present, it saves and returns the default value.
     *
     * @param key The key for the requested property value.
     * @return The value for given property key.
     */
    private String getStringProperty(String key) {
        String value = (String) PROPERTIES.get(key);
        boolean isExport = Objects.equals(value, (String) DEFAULT_PROPERTIES.get(key).getExportValue());
        if (value == null || isExport) {
            Property prop = DEFAULT_PROPERTIES.get(key);
            value = (String) prop.getValue();
            if (!isExport) {
                addProperty(key, prop);
            }
        }
        return value;
    }

    /**
     * Gets the IP adress where the server should run.
     *
     * @return The IP adress where the server should run.
     */
    public String getServerIP() {
        return getStringProperty(SERVER_ADDRESS_IP_KEY);
    }

    /**
     * Gets the port which the server should use.
     *
     * @return The port which the server should use.
     */
    public int getServerPort() {
        return getIntegerProperty(SERVER_PORT_KEY);
    }

    /**
     * Gets the size of thread pool for clients.
     *
     * @return The size of thread pool for clients.
     */
    public int getClientThreadsNumber() {
        return getIntegerProperty(SERVER_CLIENT_POOL_SIZE_KEY);
    }

    /**
     * Gets the name of the database.
     *
     * @return The name of the database.
     */
    public String getDatabaseName() {
        return getStringProperty(DATABASE_NAME_KEY);
    }

    /**
     * Gets the user for the database.
     *
     * @return The user for the database.
     */
    public String getDatabaseUser() {
        return getStringProperty(DATABASE_USER_KEY);
    }

    /**
     * Gets the password for the database.
     *
     * @return The password for the database.
     */
    public String getDatabasePassword() {
        return getStringProperty(DATABASE_PASSWORD_KEY);
    }

    /**
     * Gets the path to the location of the database.
     *
     * @return The path to the location of the database.
     */
    public String getDatabaseLocation() {
        return getStringProperty(DATABASE_LOCATION_KEY);
    }

    /**
     * Gets the initial score for a new player.
     *
     * @return The initial score for a new player.
     */
    public int getInitialScore() {
        return getIntegerProperty(INITIAL_SCORE_KEY);
    }

    /**
     * Gets the Google API key for Google Places requests.
     *
     * @return The Google API key for Google Places requests.
     */
    public String getGoogleAPIKey() {
        return getStringProperty(GOOGLE_API_KEY);
    }

    /**
     * Gets the number of random samples to determine the daytime.
     *
     * @return The number of random samples to determine the daytime.
     */
    public int getRandomPhotoSamplesNumber() {
        return getIntegerProperty(RANDOM_PHOTO_SAMPLES_KEY);
    }

    /**
     * Gets the intensity treshold for determining night photos.
     *
     * @return The intensity treshold for determining night photos.
     */
    public int getNightTreshold() {
        return getIntegerProperty(NIGHT_THRESHOLD_KEY);
    }

    /**
     * Gets the optimal width of photo for measuring similarity.
     *
     * @return The optimal width of photo for measuring similarity.
     */
    public int getOptimalWidth() {
        return getIntegerProperty(OPTIMAL_PHOTO_WIDTH_KEY);
    }

    /**
     * Gets the optimal height of photo for measuring similarity.
     *
     * @return The optimal height of photo for measuring similarity.
     */
    public int getOptimalHeight() {
        return getIntegerProperty(OPTIMAL_PHOTO_HEIGHT_KEY);
    }

    /**
     * Gets the number of segments, that the segmenter will find.
     *
     * @return The number of segments, that the segmenter will find.
     */
    public int getKmeansSegmentsNumber() {
        return getIntegerProperty(KMEANS_NUM_SEGMENTS_KEY);
    }

    /**
     * Gets the maximum number of repeats for the K-Means algorithm itself.
     *
     * @return The maximum number of repeats for the K-Means algorithm itself.
     */
    public int getKmeansMaxRepeats() {
        return getIntegerProperty(KMEANS_MAX_REPEATS_KEY);
    }

    /**
     * Gets the number of repeats when trying to find the best initial pixels.
     *
     * @return The number of repeats when trying to find the best initial
     * pixels.
     */
    public int getKmeansInitRepeats() {
        return getIntegerProperty(KMEANS_INIT_REPEATS_KEY);
    }

    /**
     * Gets if the segmenter should save debug images.
     *
     * @return True if the segmenter should save debug images.
     */
    public boolean getKmeansSaveForDebug() {
        return getIntegerProperty(KMEANS_SAVE_FOR_DEBUG_KEY) > 0;
    }

    /**
     * Number of repeats of the similarity measure algorithm.
     *
     * @return The number of repeats of the similarity measure algorithm.
     */
    public int getSimilarityNumberOfRepeats() {
        return getIntegerProperty(SIMILARITY_NUM_REPEATS);
    }

    /**
     * Gets the number of minutes to wait for thread termination.
     *
     * @return The number of minutes to wait for thread termination.
     */
    public int getPhotoPoolMaxWaitTime() {
        return getIntegerProperty(PHOTO_POOL_MAX_WAIT_TIME_KEY);
    }

    /**
     * Gets the number of threads allowed to retrieve the photos.
     *
     * @return The number of threads allowed to retrieve the photos.
     */
    public int getPhotoPoolThreadsNumber() {
        return getIntegerProperty(PHOTO_POOL_SIZE_KEY);
    }

    /**
     * Gets the number of minutes to wait for threads termination.
     *
     * @return The number of minutes to wait for threads termination.
     */
    public int getFillPoolMaxWaitTime() {
        return getIntegerProperty(FILL_POOL_MAX_WAIT_TIME_KEY);
    }

    /**
     * Gets the number of threads allowed for PlaceFiller thread pool.
     *
     * @return The number of threads allowed for PlaceFiller thread pool.
     */
    public int getFillPoolFillerThreadsNumber() {
        return getIntegerProperty(FILL_POOL_FILLER_SIZE_KEY);
    }

    /**
     * Gets the number of threads allowed to retrieve photos for each place.
     *
     * @return The number of threads allowed to retrieve photos for each place.
     */
    public int getFillPoolPhotoThreadsNumber() {
        return getIntegerProperty(FILL_POOL_PHOTO_SIZE_KEY);
    }

    /**
     * Class to store property value and its description.
     *
     * @param <T> Type of the value.
     */
    private static class Property<T> {

        private final T value;
        private final T exportValue;
        private final String description;

        /**
         * Creates a new property with given value and its description.
         *
         * @param value The value of the property.
         * @param description The description of the property.
         */
        public Property(T value, String description) {
            this.value = value;
            this.exportValue = null;
            this.description = description;
        }

        /**
         * Creates a new property with given value and its description. This
         * constructor supports private values, that should not be exported.
         *
         * @param value The value of the property.
         * @param exportValue The value which will be exported to the config
         * file, if different from the 'value'.
         * @param description The description of the property.
         */
        public Property(T value, T exportValue, String description) {
            this.value = value;
            this.exportValue = exportValue;
            this.description = description;
        }

        /**
         * Gets the value of the property.
         *
         * @return The value of the property.
         */
        public T getValue() {
            return value;
        }

        /**
         * Gets the value, which should be written to config file.
         *
         * @return The value, which should be written to config file.
         */
        public T getPublicValue() {
            return exportValue == null ? value : exportValue;
        }

        /**
         * Gets the value, which should be written to config file instead of the
         * real value.
         *
         * @return The value, which should be written to config file instead of
         * the real value.
         */
        public T getExportValue() {
            return exportValue;
        }

        /**
         * Gets the description of the property.
         *
         * @return The description of the property.
         */
        public String getDescription() {
            return description;
        }

    }

}
