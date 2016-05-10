package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.lib.entities.Photo;
import com.kappa_labs.ohunter.lib.entities.Place;
import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Request;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.requests.SearchRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For testing purposes only.
 */
public class Client {

    public static String objFile = "response.obj";

    private final String address;
    private final int port;

    
    public Client(Server server) {
        this.address = server.getAddress();
        this.port = server.getPort();
    }

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect() {
        try {
            Socket server = new Socket(address, port);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
                Request request;
                Player p = new Player(1, "nick", 4242);
//                request = new RegisterRequest("locNick", "locPasswd");
                request = new SearchRequest(
                        p, 50.0647411, 14.4196972, 20000, Photo.DAYTIME.DAY, 320, 200);
//                request = new UpdatePlayerRequest(p);
                oos.writeObject(request);
                oos.flush();
                System.out.println("Data sended...");

                ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                Object obj = ois.readObject();
                /* Serializace objektu */
                ObjectOutputStream ooss = new ObjectOutputStream(new FileOutputStream(objFile));
                ooss.writeObject(obj);
                ooss.close();
                System.out.println("Data succesfully serialized.");
                /* Test prijateho Response */
                try {
                    Response resp = (Response) obj;
                    if (resp.places != null) {
                        System.out.println("Got " + resp.places.length + " places.");
                        for (Place place : resp.places) {
                            System.out.println(place);
                        }
                    }
                    if (resp.player != null) {
                        System.out.println("Got player: " + resp.player);
                    }
                    if (resp.similarity != Float.NaN) {
                        System.out.println("Got similarity: " + resp.similarity);
                    }
                } catch (ClassCastException ex) {
                    if (obj instanceof OHException) {
                        System.err.println("Got OHException: " + obj);
                    } else {
                        System.err.println("Server sended unknown type of class.");
                    }
                }
                oos.close();
                ois.close();
//                Response resp = (Response)ois.readObject();
                server.close();
            } catch (IOException | ClassNotFoundException e) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();

        Client client = new Client(server);
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        client.connect();
    }
}
