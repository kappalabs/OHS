
package com.kappa_labs.ohunter.server;

import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import com.kappa_labs.ohunter.server.database.Database;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Request;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.LoginRequest;
import com.kappa_labs.ohunter.lib.requests.RegisterRequest;
import com.kappa_labs.ohunter.lib.requests.SearchRequest;
import com.kappa_labs.ohunter.server.net.Client;
import com.kappa_labs.ohunter.server.net.Server;
import com.kappa_labs.ohunter.server.net.requests.RequesterFactory;
import com.kappa_labs.ohunter.server.utils.PasswordUtils;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


/**
 * Provides series of test methods.
 */
public class OHunterServer {

    private static final Logger LOGGER = Logger.getLogger(OHunterServer.class.getName());
    
    private static final String RESOURCES = "./resources/";
    private static final String DARK_MODELS = RESOURCES + "models/";
    private static final String DARK = RESOURCES + "dark/";
    private static final String ANALYZER = RESOURCES + "analyzer/";
    private static final String RESULTS = "./results/";
    private static final String ANALYZER_RESULTS = RESULTS + "analyzer/";
    private static final String PHOTOS_DIR = RESULTS + "photos/";
    private static final String STATISTICS = RESULTS + "statistics.txt";
    
    private static final String[] FILES_ANALYZE;
    private static final String[] FILES_MODEL;
    private static final String[] FILES_DARK;
    
    private static final boolean 
            TEST_DATABASE = false,
            TEST_ANALYZER = false,
            TEST_NIGHT    = false,
            TEST_SERVER   = true,
            TEST_CLIENT   = false;
    
    static {
        FilenameFilter fnf = ((File dir, String name) -> name.matches(".*\\.(png|jpg|jpeg)$"));
        File fan = new File(ANALYZER);
        FILES_ANALYZE = fan.list(fnf);
        File fmo = new File(DARK_MODELS);
        FILES_MODEL = fmo.list(fnf);
        File fda = new File(DARK);
        FILES_DARK = fda.list(fnf);
    }
    
    
    /**
     * Test databazovych operaci.
     */
    private static void testDatabase() throws RemoteException {
        /* Vypis obsahu databaze */
        Database.getInstance().showTable(Database.TABLE_NAME_PLAYER);
        Player player = null;
        String nickname = "root";
        char[] passwd = "heslo".toCharArray();
        try {
            /* Registrace noveho hrace */
            Request rr = new RegisterRequest(nickname, PasswordUtils.getDigest(passwd));
            Response re_rr = RequesterFactory.buildRequester(rr).execute();
            player = re_rr.player;
            if (player != null) {
                System.out.println(player + "succesfuly registered");
            } else {
                System.err.println("player registration failed");
            }
        } catch (OHException ex1) {
            System.err.println(ex1.getMessage());
            
            /* Uzivatel je jiz registrovan, lze pouzit login */
            if (ex1.getExType() == OHException.EXType.DUPLICATE_USER) {
                try {
                    Request lr = new LoginRequest(nickname, PasswordUtils.getDigest(passwd));
                    Response re_lr = RequesterFactory.buildRequester(lr).execute();
                    player = re_lr.player;
                    if (player != null) {
                        System.out.println(player + " succesfuly logged in");
                    } else {
                        System.err.println("player login failed");
                    }
                } catch (OHException ex2) {
                    System.err.println(ex2.getMessage());
                }
            }
        }
        
        /* Po uspesne registraci lze vyuzivat ostatni requesty */
        if (player != null) {
            try {
//                Request rpr = new RejectPlaceRequest(player, "ChIJt0Fb2c6-DUcRukqmEyseueM");
//                RequesterFactory.buildRequester(rpr).execute();
                
                /* Hlinsko a okoli */
                Request sr = new SearchRequest(
                        player, 49.7621308, 15.9075567, 10000, Photo.DAYTIME.DAY, 1280, 720);
                /* Vysehrad */
//                Request sr = new SearchRequest(
//                        player, 50.0647411, 14.4196972, 200, Photo.DAYTIME.DAY, 1280, 720);
                /* Staromestske namesti */
//                Request sr = new SearchRequest(
//                        player, 50.0872842, 14.4213600, 200, Photo.DAYTIME.DAY, 1280, 720);
                
                Response re_sr = RequesterFactory.buildRequester(sr).execute();
                /* Vypis informaci o ziskanych mistech a jejich lokalni ulozeni */
                File f = new File(PHOTOS_DIR);
                if (!f.exists()) {
                    f.mkdirs();
                }
                System.out.println("saving places into " + f.getAbsolutePath() + "...");
                if (f.list() != null && f.list().length != 0) {
                    System.err.println("places directory is not empty");
                }
                
                System.out.println("List of retrieved places:");
                for (Place place : re_sr.places) {
                    System.out.println("place: "+place);
                    place.getPhotos().stream().forEach((photo) -> {
                        File outFile = new File(f, photo.generateName(64) + ".jpg");
                        try {
                            ImageIO.write(((SImage) photo.sImage).toBufferedImage(), "jpg", outFile);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    });
                }
            } catch (OHException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private static void loadImg(String fname, Photo photo) {
        try {
            photo.sImage = new SImage(ImageIO.read(new File(fname)));
            photo.reference = fname.replaceAll("\\..{3,4}$", "").replaceAll("^.*/", "");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Test rozhodovani o nocni fotografii.
     */
    private static void testNight() {
        /* Test porovnavani obrazku */
        Photo photo = new Photo();
        
        if (FILES_DARK == null) {
            System.err.println("night test needs resource images in "+DARK);
            return;
        }
        if (FILES_MODEL == null) {
            System.err.println("night test needs model images in "+DARK_MODELS);
            return;
        }
        
        for (String dark : FILES_DARK) {
            System.out.println("Dark image: "+dark);
            loadImg(DARK + dark, photo);
            if (Analyzer.isNight(photo)) {
                System.out.println(" - at night");
            } else {
                System.out.println(" - not at night");
            }
            System.out.println("--------------");
        }
    }
    
    /**
     * Test porovnavani obrazku.
     */
    private static void testAnalyzer(int numRepeats) {
        Photo ph1 = new Photo();
        Photo ph2 = new Photo();
        
        if (FILES_ANALYZE == null) {
            System.err.println("analyzer needs resource images in "+ANALYZER);
            return;
        }
        int poc = 0;
        for (int i = 0; i < FILES_ANALYZE.length; i++) {
            String fname1 = FILES_ANALYZE[i];
            System.out.println("First image: "+fname1);
            loadImg(ANALYZER + fname1, ph1);
            for (int j = i; j < FILES_ANALYZE.length; j++) {
                String fname2 = FILES_ANALYZE[j];
                System.out.println(" -> against: "+fname2);
                loadImg(ANALYZER + fname2, ph2);
                float similarity;
                try {
                    for (int k = 0; k < numRepeats; k++) {
                        similarity = Analyzer.computeSimilarity(ph1, ph2);
                        System.out.println(" -> similarity = " + similarity + " = "
                                + similarity * 100 + "%");
                        photoConnect(ANALYZER_RESULTS, ph1, ph2, (int) (similarity * 100) + "%", "_" + poc);
                        addRowAndClose(STATISTICS, (similarity * 100) + "");
                    }
                    addRowAndClose(STATISTICS, "");
                } catch (OHException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                System.out.println("-("+(i+1)+")--------------");
            }
            System.out.println("--------------");
        }
    }
    
    private static void addRowAndClose(String fileName, String row) {
        PrintWriter printWriter = null;
        try {
            File settingsFile = new File(fileName);
            printWriter = new PrintWriter(new FileOutputStream(settingsFile, true));
            printWriter.println(row);
            printWriter.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }
    
    /**
     * Connects two pictures, writes label between them. Saves them into given directory.
     * 
     * @param directory Where the resulting picture should be stored.
     * @param ph1 First photo will be on the left side.
     * @param ph2 Second photo will be on the right side.
     * @param label Label that will be in between the pictures.
     * @param suffix Suffix will be added after the whole name before file extension.
     */
    private static void photoConnect(String directory, Photo ph1, Photo ph2, String label, String suffix) {
        BufferedImage binew = new BufferedImage(ph1.getWidth() + ph2.getWidth(),
                ph1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = binew.createGraphics();
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.drawImage(((SImage)ph1._sImage).toBufferedImage(), 0, 0, null);
        graph.drawImage(((SImage)ph2._sImage).toBufferedImage(), ph1.getWidth(), 0, null);
        FontMetrics fm = graph.getFontMetrics();
        int strWidth = fm.stringWidth(label);
        graph.setColor(Color.white);
        graph.fillRect(ph1._sImage.getWidth() - strWidth/2,
                ph1._sImage.getHeight() - fm.getAscent(), strWidth, fm.getAscent());
        graph.setColor(Color.red);
        graph.drawString(label, ph1._sImage.getWidth() - strWidth/2, ph1._sImage.getHeight() - 1);
        
        
        File resultsFile = new File(directory);
        resultsFile.mkdirs();
        File outFile = new File(directory + ph1.generateName(64) + "_" + ph2.generateName(64)
                + suffix + ".jpg");
        try {
            ImageIO.write(binew, "jpg", outFile);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * Spusti hlavni funkci serveru, nasloucha klientum a obsluhuje jejich pozadavky.
     */
    private static void startServer() {
        Server server = new Server();
        server.runServer();
    }

    public static void main(String[] args) {
        if (TEST_DATABASE) {
            System.out.println("Testing database:");
            System.out.println("==============");
            try {
                testDatabase();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        if (TEST_ANALYZER) {
            System.out.println("\nTesting analyzer:");
            System.out.println("==============");
            testAnalyzer(10);
        }
        if (TEST_NIGHT) {
            System.out.println("\nTesting night recognizer:");
            System.out.println("==============");
            testNight();
        }
        if (TEST_SERVER) {
            System.out.println("\nStarting server:");
            System.out.println("================");
            startServer();
        }
        if (TEST_CLIENT) {
            System.out.println("\nStarting clients:");
            System.out.println("================");
            Client.main(null);
        }
    }
    
}
