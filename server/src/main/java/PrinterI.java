import Demo.CallbackPrx;
import Demo.Response;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;
;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrinterI implements Demo.Printer {


    private final ConcurrentHashMap<String, CallbackPrx> clients;

    public PrinterI() {
        this.clients = new ConcurrentHashMap<String, CallbackPrx>();
    }


//    /**
//     * Processes a string from a client and sends the processed string back to the client
//     * @param s the string received from the client
//     * @param client the callback to send the processed string back to the client
//     * @param current the ICE Current object
//     */
//    @Override
//    public void printString(String s, CallbackPrx client, Current current) {
//        new Thread(() -> {
//            String[] info = s.split(":", 3);
//            String newClient = info[0] + ":" + info[1];
//            clients.putIfAbsent(newClient, client);
//            Response ans = handleRequest(info[2],current);
//            System.out.println(info[0] + ":" + info[1] + ":" + ans.value);
//            System.out.println("\n");
//            client.callbackClient(new Response(0, (info[0] + ":" + info[1] + ":" + ans.value)));
//        }).start();
//    }

    @Override
    public Response executeCommand(String username, String message, Current current) {
        String result = "";
        long time = 0;
        try {
            String[] splitMessage = message.split(":", 2);
            String userHost = splitMessage[0];
            String command = splitMessage[1];
            if (command.matches("\\d+")) {
                int n = Integer.parseInt(command);
                time = System.currentTimeMillis();
                String fibonacciSeries = fibonacci(n);
                String primeFactors = primeFactors(n);
                System.out.println(userHost + ": Fibonacci series up to " + n + " is: " + fibonacciSeries);
                result = fibonacciSeries + " - Prime factors: " + primeFactors;
            } else if (command.startsWith("listifs")) {
                time = System.currentTimeMillis();
                String interfaces = listInterfaces();
                System.out.println(userHost + ": Network interfaces: " + interfaces);
                result = interfaces;
            } else if (command.startsWith("listports")) {
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    String ipAddress = parts[1];
                    time = System.currentTimeMillis();
                    result = listOpenPorts(ipAddress);
                    System.out.println(userHost + ": Open ports for " + ipAddress + ": " + result);
                } else {
                    result = "Error: No IP address provided.";
                }
            } else if (command.startsWith("!")) {
                String cmd = command.substring(1);
                time = System.currentTimeMillis();
                result = executeCmd(cmd);
                System.out.println(userHost + ": Command execution result: " + result);
            } else {
                result = "Unknown command.";
                System.out.println(userHost + ": " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error executing command:" + e.getMessage();
        }
        if (time == 0) {
            return new Response(0, result);
        }
        long timetotal = System.currentTimeMillis() - time;
        return new Response(timetotal, result);
    }

    public static String fibonacci(int n) {
        List<BigInteger> fibSeries = new ArrayList<>();
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;

        while (n-- > 0) {
            fibSeries.add(a);
            BigInteger temp = a.add(b);
            a = b;
            b = temp;
        }

        return fibSeries.toString();
    }

    public static String primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.toString();
    }

    public static String listInterfaces() throws java.net.SocketException {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            sb.append(netint.getDisplayName()).append("\n");
        }
        return sb.toString();
    }

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

    public static String executeCmd(String command) {
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


    @Override
    public String join(String username, CallbackPrx callback, Current current) {
        if (!clients.containsKey(username)) {
            clients.put(username, callback);
            return "User added: " + username;
        } else {
            return "Username already taken: " + username;
        }
    }

    @Override
    public String listUsernames(Current current) {
        String usernames = "";
        for (String username : clients.keySet()) {
            usernames += username + "\n";
        }
        return usernames;
    }

    @Override
    public void broadcastMessage(String sender, String message, Current current) {
        Collection<CallbackPrx> callbacks = clients.values();
        for (CallbackPrx callback : callbacks) {
            try {
                callback.receiveMessage(sender + ": " + message);
            } catch (Exception e) {
                System.err.println("Failed to send message to one of the users" + e.getMessage());
            }
        }
    }

    @Override
    public void sendMessage(String sender, String s, String receptor, Current current) {
        CallbackPrx callback = clients.get(receptor);
        if (callback != null) {
            callback.receiveMessage(sender + " [PRIVATE]: " + s);
        } else {
            System.out.println("User " + receptor + " not found!");
        }
    }

    @Override
    public String leave(String username, Current current) {
        if (clients.containsKey(username)) {
            clients.remove(username);
            return "User removed:" + username;
        }
        return "User not found:" + username;
    }


}