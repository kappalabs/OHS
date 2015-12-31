
package com.kappa_labs.ohunter.server.net;

import com.kappa_labs.ohunter.net.OHException;
import com.kappa_labs.ohunter.net.Response;
import com.kappa_labs.ohunter.requests.Request;
import com.kappa_labs.ohunter.requests.RequestPkg;
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

    public Client() {
        try {
            Socket server = new Socket("192.168.1.196", 4242);
            try {
                System.out.println("Pred oos");
                ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
                System.out.println("Data na server");
                RequestPkg rp = new RequestPkg(Request.TYPE.REGISTER);
                rp.setParams(new Object[]{"nickClient", "passwdClient"});
                oos.writeObject(rp);
                oos.flush();
                System.out.println("Data OK odeslana");
                
                System.out.println("Pred ois");
                ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                Object obj = ois.readObject();
                try {
                    Response resp = (Response) obj;
                    System.out.println(resp.player.toString());
                } catch (ClassCastException ex) {
                    if (obj instanceof OHException) {
                        System.err.println("VYpadla mi OHExceptiona: "+obj);
                    } else {
                        System.err.println("Server posila neznamy format tridy");
                    }
                }
                oos.close();
                ois.close();
//                        Response resp = (Response)ois.readObject();
                server.close();
            } catch (IOException | ClassNotFoundException e) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
