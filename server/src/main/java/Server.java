import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import java.io.*;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;


public class Server
{

    /**
     * The main entry point for the server.
     *
     * <p>This program starts an Ice object adapter and waits for the
     * adapter to be shut down. This is the simplest way to start an
     * Ice server; the adapter will be destroyed when the program
     * terminates.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int status = 0;
        Communicator communicator = null;
        try {
            communicator = Util.initialize(args, "config.server");
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("ServerAdapter", "default -p 9099");
            Object object = new PrinterI();
            adapter.add((com.zeroc.Ice.Object) object, Util.stringToIdentity("Server"));
            adapter.activate();
            System.out.println("Server started...");
            communicator.waitForShutdown();
        } catch (Exception e) {

        }
    }


}