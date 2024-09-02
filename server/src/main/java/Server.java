import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import java.io.*;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


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
            communicator = Util.initialize(args);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("ServerAdapter", "default -p 10000");
            Object object = new PrinterI();
            adapter.add((com.zeroc.Ice.Object) object, Util.stringToIdentity("SimpleServer"));
            adapter.activate();
            System.out.println("Server started...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
            status = 1;
        }
        if (communicator != null) {
            communicator.destroy();
        }
        System.exit(status);
    }
    /**
     * Generates the Fibonacci series up to n.
     *
     * @param n The number of elements to generate in the series
     * @return The Fibonacci series as a string
     */
    public static String fibonacci(int n) {
        // Genera la serie de Fibonacci hasta n
        List<Integer> fibSeries = new ArrayList<>();
        int a = 0, b = 1;
        while (n-- > 0) {
            fibSeries.add(a);
            int temp = a + b;
            a = b;
            b = temp;
        }
        return fibSeries.toString();
    }
    /**
     * Calculates the prime factors of n.
     *
     * @param n the number to factor
     * @return a list of the prime factors of n
     */
    public static String primeFactors(int n) {
        // Calcula los factores primos de n
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.toString();
    }

    /**
     * Returns a string containing the display names of all available network
     * interfaces on the system, one per line.
     *
     * @return a string containing the display names of all available network
     *         interfaces
     * @throws SocketException if an error occurs while retrieving the list of
     *         network interfaces
     */
    public static String listInterfaces() throws java.net.SocketException {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            sb.append(netint.getDisplayName()).append("\n");
        }
        return sb.toString();
    }
    /**
     * Executes nmap to scan open ports in the given IP address.
     * @param ipAddress the IP address to scan
     * @return a string containing the output of nmap
     */
    public static String listOpenPorts(String ipAddress) {
        StringBuilder output = new StringBuilder();
        try {
            // Ejecuta el comando nmap para escanear los puertos abiertos
            Process process = Runtime.getRuntime().exec("nmap -p- " + ipAddress);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            output.append("Error executing nmap.");
        }
        return output.toString();
    }


    /**
     * Executes a command and returns its output.
     * @param command the command to execute
     * @return the output of the command
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            output.append("Error executing command.");
        }
        return output.toString();
    }

}