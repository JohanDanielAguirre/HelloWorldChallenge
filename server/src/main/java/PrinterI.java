import Demo.CallbackPrx;
import Demo.Response;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PrinterI implements Demo.Printer {
    private final AtomicInteger sucees = new AtomicInteger(0);
    private final AtomicInteger error = new AtomicInteger(0);
    private final AtomicInteger trhougput = new AtomicInteger(0);
    private final AtomicLong totaltime = new AtomicLong(0);



    private final ConcurrentHashMap<String, CallbackPrx> clients;

    public PrinterI() {
        this.clients = new ConcurrentHashMap<String, CallbackPrx>();
    }

    @Override
    public Response executeCommand(String userHost, String command, Current current) {
        if (command.startsWith("generate_report")) {
            int total = 0;
            total +=    sucees.get() + error.get()+ trhougput.get();
            long timetotal = totaltime.get();
            int successrequest = sucees.get();
            int errorrequest = error.get();
            int throughput = trhougput.get();
            long average = (long) (timetotal / total);
            sucees.set(0);
            error.set(0);
            trhougput.set(0);
            totaltime.set(0);
            return new Response(0, "Success: " + successrequest +"\n"
                    + " - Error: " + errorrequest+"\n"
                    + " - Throughput: " + throughput+"\n"
                    + "Total requests "+ total + "\n"
                    + " - Total time: " + timetotal + "\n"
                    +"average time: " + average + "\n");
        }
        String result = "";
        long time = 0;
        try {
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
            }
            else if (command.equals("list clients")) {
                time = System.currentTimeMillis();
                result = listUsernames(current);
            } else if (command.startsWith("to ")) {
                String[] parts = command.split(" ", 3);
                if (parts.length == 3) {
                    String receptor = parts[1];
                    String message = parts[2];
                    time = System.currentTimeMillis();
                    sendMessage(userHost, message, receptor, current);
                } else {
                    result = "Usage: to <username> <message>";
                }
            } else if (command.startsWith("BC:")) {
                String message = command.substring(3);
                time = System.currentTimeMillis();
                broadcastMessage(userHost, message, current);
            }
            else {
                result = "Unknown command.";
                System.out.println(userHost + ": " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error executing command:" + e.getMessage();
            error.incrementAndGet();
        }
        if (time == 0) {
            trhougput.incrementAndGet();
            return new Response(0, result);
        }
        long timetotal = System.currentTimeMillis() - time;
        sucees.incrementAndGet();
        totaltime.addAndGet(timetotal);
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