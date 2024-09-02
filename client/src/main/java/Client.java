import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import java.net.InetAddress;
import java.util.Scanner;

public class Client
{
    public static void main(String[] args) {
        int status = 0;
        Communicator communicator = null;
        try {
            communicator = Util.initialize(args);
            ObjectPrx base = communicator.stringToProxy("SimpleServer:default -p 10000");
            PrinterPrx server = PrinterPrx.checkedCast(base);
            if (server == null) throw new Error("Invalid proxy");

            Scanner scanner = new Scanner(System.in);
            String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();

            while (true) {
                System.out.print("Enter a message (or 'exit' to quit): ");
                String input = scanner.nextLine();
                if (input.equals("exit")) {
                    break;
                }

                String message = username + "@" + hostname + ":" + input;
                long time= System.currentTimeMillis();
                Response response = (server.printString(message));
                System.out.println("Server response: " + response.value);
                System.out.println("Time taken total: " + (System.currentTimeMillis() - time) + "ms");
                System.out.println("Received response or processing time of " + response.responseTime + "ms");
                System.out.println("latency is " + (System.currentTimeMillis() - time - response.responseTime) + "ms");
            }

        } catch (Exception e) {
            e.printStackTrace();
            status = 1;
        }
        if (communicator != null) {
            communicator.destroy();
        }
        System.exit(status);
    }
}