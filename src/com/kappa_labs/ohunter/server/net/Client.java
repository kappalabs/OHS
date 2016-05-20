package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Request;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.BestPlayersRequest;
import com.kappa_labs.ohunter.lib.requests.FillPlacesRequest;
import com.kappa_labs.ohunter.lib.requests.LoginRequest;
import com.kappa_labs.ohunter.lib.requests.RadarSearchRequest;
import com.kappa_labs.ohunter.lib.requests.RegisterRequest;
import com.kappa_labs.ohunter.lib.requests.SearchRequest;
import com.kappa_labs.ohunter.lib.requests.UpdatePlayerRequest;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For testing purposes only.
 */
public class Client {

    private final String address;
    private final int port;
    
    private static AtomicInteger[] requestsNum, resultsNum;
    private static AtomicInteger exceptionsResults;

    
    public Client(Server server) {
        this.address = server.getAddress();
        this.port = server.getPort();
    }

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }
    
    private static void initAtomicValues() {
        requestsNum = new AtomicInteger[5];
        resultsNum = new AtomicInteger[requestsNum.length];
        for (int i = 0; i < requestsNum.length; i++) {
            requestsNum[i] = new AtomicInteger();
            resultsNum[i] = new AtomicInteger();
        }
        exceptionsResults = new AtomicInteger();
    }

    /**
     * Connect and exchange information with server.
     * 
     * @param request Request for the server.
     */
    public void connect(Request request) {
        try {
            Socket server = new Socket(address, port);
            ObjectOutputStream oos;
            ObjectInputStream ois;
            try {
                oos = new ObjectOutputStream(server.getOutputStream());
                oos.writeObject(request);
                oos.flush();

                ois = new ObjectInputStream(server.getInputStream());
                Object obj = ois.readObject();
                /* Test pocita prijate Response a OHException */
                try {
                    Response resp = (Response) obj;
                    if (resp.player != null) {
                        resultsNum[0].getAndIncrement();
                    }
                    if (!Float.isNaN(resp.similarity)) {
                        resultsNum[1].getAndIncrement();
                    }
                    if (resp.photos != null) {
                        resultsNum[2].getAndIncrement();
                    }
                    if (resp.places != null) {
                        resultsNum[3].getAndIncrement();
                    }
                    if (resp.players != null) {
                        resultsNum[4].getAndIncrement();
                    }
                } catch (ClassCastException ex) {
                    if (obj instanceof OHException) {
                        System.err.println("Got OHException: " + ((OHException) obj).getMessage());
                        exceptionsResults.getAndIncrement();
                    } else {
                        System.err.println("Server sended unknown type of class.");
                    }
                }
                oos.close();
                ois.close();
                server.close();
            } catch (ConnectException ex) {
                System.out.println(ex);
            } catch (IOException | ClassNotFoundException e) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runInBackground();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        /* Pocet testovacich klientu */
        int numClients = 10;
        
        initAtomicValues();
        System.out.println("Number of clients: " + numClients);
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        for (int i = 0; i < numClients; i++) {
            executor.submit(new ClientWorker(new Client(server)));
        }
        executor.shutdown();

        System.out.println("executor: " + executor);
        try {
            System.out.println("Awaiting for termination of client workers...");
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("======================================");
        System.out.println("     RESULTS (requests x results)     ");
        System.out.println("======================================");
        /* All requests should return Player object back */
        System.out.println("Number of player:     " + requestsNum[0] + " x(" + resultsNum[0] + ")");
        /* Only some of them should return other objects */
        System.out.println("Number of similarity: " + requestsNum[1] + " x " + resultsNum[1]);
        System.out.println("Number of photos:     " + requestsNum[2] + " x " + resultsNum[2]);
        System.out.println("Number of places:     " + requestsNum[3] + " x " + resultsNum[3]);
        System.out.println("Number of players:    " + requestsNum[4] + " x " + resultsNum[4]);
        System.out.println("Number of exceptions: " + exceptionsResults);
        server.shutDown();
    }

    private static class ClientWorker implements Runnable {

        private final Client mClient;
        
        private static final List<Request> requests;
        private static final Random random = new Random();
        private static final List<Integer> requestType;

        static {
            requests = new ArrayList<>();
            requestType = new ArrayList<>();
            
            Player player = new Player(1, "nick", 4242);
            
            /* Player requests */
            requests.add(new RegisterRequest("testNick", "testPasswd"));
            requestType.add(0);
            requests.add(new LoginRequest("testNick", "testPasswd"));
            requestType.add(0);
            requests.add(new UpdatePlayerRequest(player));
            requestType.add(0);
            /* Similarity requests */
            /* Photos requests */
            /* Places requests */
            requests.add(new SearchRequest(
                    player, 50.0647411, 14.4196972, 20000, Photo.DAYTIME.DAY, 320, 200));
            requestType.add(3);
            requests.add(new RadarSearchRequest(player, 50.0647411, 14.4196972, 20000));
            requestType.add(3);
            requests.add(new FillPlacesRequest(player, new String[]{
                "ChIJ70BFLOyUC0cRJiDPlgy0NNM",
                "ChIJZ6M-R6OUC0cRDPZZeMrSwt4",
                "ChIJI9D08-OUC0cRY0ZsDDwPw1Q",
                "ChIJw6fqIvWUC0cRE9PcP3cxHYc",
                "ChIJAyNP5o6UC0cRH2h_j6e-d9c"},
                Photo.DAYTIME.UNKNOWN,
                320, 480));
            requestType.add(3);
            /* Players requests */
            requests.add(new BestPlayersRequest(player, 50));
            requestType.add(4);
        }
        
        
        public ClientWorker(Client mClient) {
            this.mClient = mClient;
        }
        
        private static Request getRequest(int requestIndex) {
            requestsNum[requestType.get(requestIndex)].getAndIncrement();
            return requests.get(requestIndex);
        }
        
        private static Request getRandomRequest() {
            int requestIndex = random.nextInt(requests.size());
            return getRequest(requestIndex);
        }
        
        @Override
        public void run() {
            mClient.connect(getRandomRequest());
        }

    }

}
