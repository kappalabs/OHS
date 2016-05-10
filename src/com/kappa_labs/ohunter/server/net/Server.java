
package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.lib.entities.Player;
import com.kappa_labs.ohunter.lib.net.Response;
import com.kappa_labs.ohunter.lib.net.OHException;
import com.kappa_labs.ohunter.lib.net.Request;
import com.kappa_labs.ohunter.server.database.Database;
import com.kappa_labs.ohunter.server.net.requests.RequesterFactory;
import com.kappa_labs.ohunter.server.net.requests.SearchRequester;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class providing the server actions, services for clients.
 */
public class Server {

    public static final int PORT = 4242;
    public static final int NUM_THREADS = 8;
    
    private String address;
    private int port;
    
    private final boolean debugCache = false;
    
    
    /**
     * Start the server, listen to clients and fulfill their requests.
     */
    public void runServer() {
        ServerSocket server = null;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        
        try {
            server = new ServerSocket();
            address = findServerIP();
            port = PORT;
            SocketAddress addr = new InetSocketAddress(address, port);
            server.bind(addr);
            System.out.println("name: "+addr.toString());
        
            ServerService ss = new ServerService(executor, server);
            Thread servThread = new Thread(ss);
            servThread.setDaemon(true);
            servThread.start();
            
            System.out.println("Jdu do smycky...");
            while(true) {
                Socket client = server.accept();
                try {
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    System.out.println("\nMam noveho klienta");
                    Request request = null;
                    try {
                        request = (Request) ois.readObject();
                        System.out.println("Request info: "+request);
                    } catch (InvalidClassException icex) {
                        System.err.println("Serializace je inkompatibilni!");
                    }
                    request = RequesterFactory.buildRequester(request);
                    ClientWorker cw = new ClientWorker(request, oos, client);
                    executor.execute(cw);
                    System.out.println("Request odeslan executoru");
                } catch (IOException ex) {
                    System.err.println(ex);
                    Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            /* Belongs to server.accept() */
            if (ex instanceof SocketException) {
                /* OK, client closed */
                return;
            }
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Database.getInstance().closeDatabase();
            if (server != null) {
                try {
                    server.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
    }
    
    private String findServerIP() {
        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            List<String> adds = new ArrayList<>();
            adds.add("custom");
            for (; n.hasMoreElements();) {
                NetworkInterface e = n.nextElement();
                
                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements();) {
                    InetAddress addr = a.nextElement();
                    adds.add(addr.getHostAddress());
                }
            }
            
            System.out.println("Available IPs:");
            for (int i = 0; i < adds.size(); i++) {
                    System.out.println("[" + i + "] " + adds.get(i));
            }
            
            System.out.print("Choose server IP address index: ");
            Scanner sc = new Scanner(System.in);
            try {
                int index = sc.nextInt();
                if (index > 0 && index < adds.size()) {
                    return adds.get(index);
                } else if (index == 0) {
                    System.out.print("Write the custom address: ");
                    String in = sc.next();
                    return in;
                }
            } catch (InputMismatchException ex) {
                System.err.println("Input must be integer!");
            }
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "localhost";
    }

    /**
     * Gets the current server address.
     * 
     * @return The current server address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the current server port.
     * 
     * @return The current server port.
     */
    public int getPort() {
        return port;
    }
    
    private class ServerService implements Runnable {
        
        private final String PROMPT = "SS: ";
        
        private final ExecutorService mExecutor;
        private final ServerSocket mServer;

        
        public ServerService(ExecutorService mExecutor, ServerSocket mServer) {
            this.mExecutor = mExecutor;
            this.mServer = mServer;
        }

        @Override
        public void run() {
            System.out.println("running ServerService");
            Scanner sc = new Scanner(System.in);
            
            String text;
            System.out.print(PROMPT);
            while ((text = sc.nextLine()) != null) {
                String args[] = text.trim().split("\\s+");
                switch (args[0].toLowerCase()) {
                    case "help":
                        System.out.println("ServerService: help\n"
                                + "help - prints this message\n"
                                + "state - prints state of the thread pool\n"
                                + "database - tries to initialize the database\n"
                                + "table show 'name' - prints whole database table with given name\n"
                                + "player remove 'playerID' - deletes player with given ID from the database\n"
                                + "player setscore 'playerID' 'score' - sets score of player with given ID\n"
                                + "player setname 'playerID' 'name' - sets name of player with given ID\n"
                                + "bests 'count' - prints the 'count' best players\n"
                                + "exit - terminate threads and shutdown the server");
                        break;
                    case "state":
                        System.out.println("ServerService: " + mExecutor.toString());
                        break;
                    case "database":
                        Database.getInstance().tryInitConnection();
                        break;
                    case "table":
                        if (args.length < 3) {
                            System.out.println("ServerService: unknown command, try 'help'");
                            break;
                        }
                        switch (args[1].toLowerCase()) {
                            case "show":
                                Database.getInstance().showTable(args[2]);
                                break;
                            default:
                                System.out.println("ServerService: unknown command, try 'help'");
                        }
                        break;
                    case "player":
                        if (args.length < 3) {
                            System.out.println("ServerService: unknown command, try 'help'");
                            break;
                        }
                        try {
                        int playerID = Integer.parseInt(args[2]);
                            switch (args[1].toLowerCase()) {
                                case "remove":
                                    Database.getInstance().removePlayer(playerID);
                                    break;
                                case "setscore":
                                    if (args.length < 4) {
                                        System.out.println("ServerService: unknown command, try 'help'");
                                        break;
                                    }
                                    int score = Integer.parseInt(args[3]);
                                    Database.getInstance().editPlayer(playerID, null, score, null);
                                    break;
                                case "setname":
                                    Database.getInstance().editPlayer(playerID, args[3], null, null);
                                    break;
                                default:
                                    System.out.println("ServerService: unknown command, try 'help'");
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("ServerService: wrong number, try 'help'");
                        }
                        break;
                    case "bests":
                        if (args.length < 2) {
                            System.out.println("ServerService: unknown command, try 'help'");
                            break;
                        }
                        int count = Integer.parseInt(args[1]);
                        Player[] bests = Database.getInstance().getBestPlayers(count);
                        System.out.println("Best players:");
                        for (int i = 0; i < bests.length && i < count; i++) {
                            Player best = bests[i];
                            System.out.println(best);
                        }
                        break;
                    case "exit":
                        shutdown();
                        sc.close();
                        System.out.println("ServerService: all threads down");
                        return;
                    default:
                        System.out.println("ServerService: unknown command, try 'help'");
                }
                System.out.print(PROMPT);
            }
        }
        
        private void shutdown() {
            mExecutor.shutdown();
            while(!mExecutor.isTerminated()) {
                /* Wait for termination of all threads */
            }
            try {
                mServer.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        
    }
    
    private class ClientWorker implements Runnable {
        
        private final Socket mClient;
        private final Request mRequest;
        private final ObjectOutputStream mOutput;

        
        public ClientWorker(Request request, ObjectOutputStream outputStream, Socket client) {
            this.mRequest = request;
            this.mOutput = outputStream;
            this.mClient = client;
        }

        @Override
        public void run() {
            try {
                System.out.println("Request prijmut k provedeni");
                Response response;
                if (debugCache && mRequest instanceof SearchRequester) {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Client.objFile));
                    response = (Response) ois.readObject();
                } else {
                    response = mRequest.execute();
                }
                System.out.println("request spocitan, odesilam...");
                if (response != null) {
                    System.out.println("Response info: " + response);
                    System.out.println("Vyrizovani trvalo cca "
                            + String.format("%.2fs", (response.getTimestamp().getTime() - mRequest.getTimestamp().getTime()) / 1000.0));
                }
                mOutput.writeObject(response);
                System.out.println("response odeslan, klient obslouzen");
            } catch (OHException ex) {
                try {
//                    Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
                    System.err.println(ex);
                    System.out.println("Vypadla vyjimka!! odeslu ji...");
                    mOutput.writeObject(ex);
                    System.out.println("Odeslano, klient obslouzen");
                } catch (IOException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex1);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                System.out.println("---------------------------");
                try {
                    mOutput.close();
                    mClient.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
