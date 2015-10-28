
package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.server.database.Database;
import com.kappa_labs.ohunter.server.net.requests.Request;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
    public static final int NUM_THREADS = 4;
    
    /**
     * Start the server, listen to clients and fulfill their requests.
     */
    public void runServer() {
        ServerSocket server = null;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        
        
        try {
            server = new ServerSocket();
            SocketAddress addr = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), PORT);
            server.bind(addr);
        
            ServerService ss = new ServerService(executor, server);
            Thread servThread = new Thread(ss);
            servThread.setDaemon(true);
            servThread.start();
            
            while(true) {
                Socket client = server.accept();
                try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                     ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())
                    ) {
                    Request request = (Request) ois.readObject();
                    ClientWorker cw = new ClientWorker(request, oos);
                    executor.execute(cw);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
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
    
    private class ServerService implements Runnable {
        
        private ExecutorService mExecutor;
        private ServerSocket mServer;

        
        public ServerService(ExecutorService mExecutor, ServerSocket mServer) {
            this.mExecutor = mExecutor;
            this.mServer = mServer;
        }

        @Override
        public void run() {
            System.out.println("running ServerService");
            Scanner sc = new Scanner(System.in);
            
            String text;
            System.out.print("SS: ");
            while ((text = sc.next()) != null) {
                switch(text.toLowerCase().trim()) {
                    case "help":
                        System.out.println("ServerService: help\n"
                                + "help - prints this message\n"
                                + "state - prints state of the thread pool\n"
                                + "exit - terminate threads and shutdown the server");
                        break;
                    case "state":
                        System.out.println("ServerService: " + mExecutor.toString());
                        break;
                    case "exit":
                        mExecutor.shutdown();
                        while(!mExecutor.isTerminated()) {
                            /* Wait for termination of all threads */
                        }
                        try {
                            mServer.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
                        }
                        System.out.println("ServerService: all threads down");
                        return;
                    default:
                        System.out.println("ServerService: unknown command");
                }
                System.out.print("SS: ");
            }
        }
        
    }
    
    private class ClientWorker implements Runnable {
        
        private Request mRequest;
        private ObjectOutputStream mOutput;

        
        public ClientWorker(Request request, ObjectOutputStream outputStream) {
            this.mRequest = request;
            this.mOutput = outputStream;
        }

        @Override
        public void run() {
            try {
                Response response = mRequest.execute();
                mOutput.writeObject(response);
            } catch (OHException ex) {
                try {
                    mOutput.writeObject(ex);
                } catch (IOException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex1);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        
        
    }
    
}
