
package com.kappa_labs.ohunter.server;

import com.kappa_labs.ohunter.server.entities.SImage;
import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.server.analyzer.Analyzer;
import com.kappa_labs.ohunter.server.database.Database;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.server.net.Client;
import com.kappa_labs.ohunter.server.net.Server;
import com.kappa_labs.ohunter.server.net.requests.LoginRequest;
import com.kappa_labs.ohunter.server.net.requests.RegisterRequest;
import com.kappa_labs.ohunter.server.net.requests.SearchRequest;
import com.kappa_labs.ohunter.server.utils.PasswordUtils;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Provides series of test methods.
 */
public class OHunterServer {
    
    private static final String RESOURCES = "./resources/";
    private static final String DARK_MODELS = RESOURCES + "models/";
    private static final String DARK = RESOURCES + "dark/";
    private static final String ANALYZER = RESOURCES + "analyzer/";
    private static final String RESULTS = "./results/";
    private static final String ANALYZER_RESULTS = RESULTS + "analyzer/";
    private static final String MODEL_RESULTS = RESULTS + "models/";
    
    private static String[] fanalyze;
    private static String[] fdmodels;
    private static String[] fdark;
    
    static {
        FilenameFilter fnf = ((File dir, String name) -> name.matches(".*\\.(png|jpg|jpeg)$"));
        File fan = new File(ANALYZER);
        fanalyze = fan.list(fnf);
        File fmo = new File(DARK_MODELS);
        fdmodels = fmo.list(fnf);
        File fda = new File(DARK);
        fdark = fda.list(fnf);
    }
    
    private static void testDatabase() throws RemoteException {
        /* Vypis obsahu databaze */
        Database.getInstance().showPlayers();
        Player player = null;
        String nickname = "root";
        char[] passwd = "heslo".toCharArray();
        try {
            /* Registrace noveho hrace */
            RegisterRequest rr = new RegisterRequest(nickname, PasswordUtils.getDigest(passwd));
            Response re_rr = rr.execute();
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
                    LoginRequest lr = new LoginRequest(nickname, PasswordUtils.getDigest(passwd));
                    Response re_lr = lr.execute();
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
//                RejectPlaceRequest rpr = new RejectPlaceRequest(player, "ChIJt0Fb2c6-DUcRukqmEyseueM");
//                rpr.execute();
                
                // Hlinsko a okoli
                SearchRequest sr = new SearchRequest(
                        player, 49.7621308, 15.9075567, 10000, Photo.DAYTIME.DAY, 1280, 720);
                // Vysehrad
//                SearchRequest sr = new SearchRequest(
//                        player, 50.0647411, 14.4196972, 200, Photo.DAYTIME.DAY, 1280, 720);
                // Staromestske namesti
//                SearchRequest sr = new SearchRequest(
//                        player, 50.0872842, 14.4213600, 200, Photo.DAYTIME.DAY, 1280, 720);
                
                Response re_sr = sr.execute();
                /* Vypis informaci o ziskanych mistech a jejich lokalni ulozeni */
                System.out.println("saving places into ./" + Place.PHOTOS_DIR + "...");
                File f = new File(Place.PHOTOS_DIR);
                if (f.list() != null && f.list().length != 0) {
                    System.err.println("places directory is not empty");
                }
                
                System.out.println("List of retrieved places:");
                re_sr.places.stream().forEach((place) -> {
                    System.out.println("place: "+place);
//                    place.saveToFile(null);
//TODO: novy save to file pristup
                });
            } catch (OHException ex) {
                System.err.println(ex.getMessage());
            }
        }
        Database.getInstance().closeDatabase();
    }
    
    private static void loadImg(String fname, Photo photo) {
        try {
            photo.image = new SImage(ImageIO.read(new File(fname)));
            photo.reference = fname.replaceAll("\\..{3,4}$", "").replaceAll("^.*/", "");
        } catch (IOException ex) {
            Logger.getLogger(OHunterServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Test rozhodovani o nocni fotografii.
     */
    private static void testNight() {
        /* Test porovnavani obrazku */
        Photo ph1 = new Photo();
        Photo ph2 = new Photo();
        
        if (fdark == null) {
            System.err.println("night test needs resource images in "+DARK);
            return;
        }
        if (fdmodels == null) {
            System.err.println("night test needs model images in "+DARK_MODELS);
            return;
        }
        
        for (String dark : fdark) {
            System.out.println("Dark image: "+dark);
            loadImg(DARK + dark, ph1);
            if (Analyzer.isNight(ph1)) {
                System.out.println(" - at night");
            } else {
                System.out.println(" - not at night");
            }
            for (String model : fdmodels) {
                System.out.println(" -> against model: "+model);
                loadImg(DARK_MODELS + model, ph2);
                float similarity = Analyzer.computeSimilarity(ph1, ph2);
                System.out.println(" -> similarity = " + similarity + ", pc = "
                        + (1.f - similarity)*100 + "%");
                photoConnect(ANALYZER_RESULTS, ph1, ph2, (int)((1.f - similarity)*100)+"%");
                System.out.println("--------------");
            }
            System.out.println("--------------");
        }
    }
    
    /**
     * Test porovnavani obrazku
     */
    private static void testAnalyzer() {
        Photo ph1 = new Photo();
        Photo ph2 = new Photo();
        
        if (fanalyze == null) {
            System.err.println("analyzer needs resource images in "+ANALYZER);
            return;
        }
        for (String fname1 : fanalyze) {
            System.out.println("First image: "+fname1);
            loadImg(ANALYZER + fname1, ph1);
            for (String fname2 : fanalyze) {
                System.out.println(" -> against: "+fname2);
                loadImg(ANALYZER + fname2, ph2);
                float similarity = Analyzer.computeSimilarity(ph1, ph2);
                System.out.println(" -> similarity = " + similarity + ", pc = "
                        + (1.f - similarity)*100 + "%");
//                photoConnect(ANALYZER_RESULTS, ph1, ph2, (int)((1.f - similarity)*100)+"%");
                photoConnect(ANALYZER_RESULTS, ph1, ph2, (int)(similarity*10) + "");
//                photoConnect(ANALYZER_RESULTS, ph1, ph2, similarity + "");
                System.out.println("--------------");
            }
            System.out.println("--------------");
        }
    }
    
    /*
     * spoji a ohodnoti obrazky, ulozi do souboru
    */
    private static void photoConnect(String directory, Photo ph1, Photo ph2, String label) {
        BufferedImage binew = new BufferedImage(ph1.getWidth() + ph2.getWidth(),
                ph1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = binew.createGraphics();
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.drawImage(((SImage)ph1._image).toBufferedImage(), 0, 0, null);
        graph.drawImage(((SImage)ph2._image).toBufferedImage(), ph1.getWidth(), 0, null);
        FontMetrics fm = graph.getFontMetrics();
        int strWidth = fm.stringWidth(label);
        graph.setColor(Color.white);
        graph.fillRect(ph1._image.getWidth() - strWidth/2,
                ph1._image.getHeight() - fm.getAscent(), strWidth, fm.getAscent());
        graph.setColor(Color.red);
        graph.drawString(label, ph1._image.getWidth() - strWidth/2, ph1._image.getHeight() - 1);
        
        
        File resultsFile = new File(directory);
        resultsFile.mkdirs();
        File outFile = new File(directory + ph1.reference + "_" + ph2.reference);
        try {
            ImageIO.write(binew, "png", outFile);
        } catch (IOException ex) {
            Logger.getLogger(OHunterServer.class.getName()).log(Level.WARNING, null, ex);
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
//        System.out.println("Testing database:");
//        System.out.println("==============");
//        testDatabase();
//        System.out.println("\nTesting analyzer:");
//        System.out.println("==============");
//        testAnalyzer();
        System.out.println("\nTesting night recognizer:");
        System.out.println("==============");
        testNight();
//        System.out.println("\nTesting server:");
//        System.out.println("==============");
//        startServer();
//        
//        Client c = new Client();
    }
    
}
