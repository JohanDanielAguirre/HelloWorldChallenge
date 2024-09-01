import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PrinterI implements Demo.Printer
{
    public Response printString(String s, com.zeroc.Ice.Current current)
    {
        System.out.println(s);
        return new Response(0, "Server response: " + s);
    }

    public String processMessage(String message, Current __current) {
        String result = "";
        try {
            String[] splitMessage = message.split(":", 2);
            String userHost = splitMessage[0];
            String command = splitMessage[1];

            if (command.matches("\\d+")) {
                // Si el mensaje es un número entero positivo
                int n = Integer.parseInt(command);
                String fibonacciSeries = fibonacci(n);
                String primeFactors = primeFactors(n);
                System.out.println(userHost + ": Fibonacci series up to " + n + " is: " + fibonacciSeries);
                result = fibonacciSeries + " - Prime factors: " + primeFactors;
            } else if (command.startsWith("listifs")) {
                String interfaces = listInterfaces();
                System.out.println(userHost + ": Network interfaces: " + interfaces);
                result = interfaces;
            } else if (command.startsWith("listports")) {
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    String ipAddress = parts[1];
                    result = listOpenPorts(ipAddress);
                    System.out.println(userHost + ": Open ports for " + ipAddress + ": " + result);
                } else {
                    result = "Error: No IP address provided.";
                }
            } else if (command.startsWith("!")) {
                String cmd = command.substring(1);
                result = executeCommand(cmd);
                System.out.println(userHost + ": Command execution result: " + result);
            } else {
                result = "Unknown command.";
                System.out.println(userHost + ": " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error processing message.";
        }
        return result;
    }

    private String fibonacci(int n) {
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

    private String primeFactors(int n) {
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

    private String listInterfaces() throws java.net.SocketException {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            sb.append(netint.getDisplayName()).append("\n");
        }
        return sb.toString();
    }

    private String listOpenPorts(String ipAddress) {
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

    private String executeCommand(String command) {
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
}