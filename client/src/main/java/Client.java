import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import Demo.CallbackPrx;
import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;


public class Client{
    public static void main(String[] args) {
        try(Communicator communicator = Util.initialize(args, "config.client")){
            PrinterPrx server = PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));
            if (server == null) throw new Error("Invalid proxy");

            ObjectAdapter adapter = communicator.createObjectAdapter("Client");
            CallbackImpl callbackI = new CallbackImpl();
            ObjectPrx callbackBase = adapter.add(callbackI, Util.stringToIdentity("callback"));
            adapter.activate();

            CallbackPrx callbackPrx = CallbackPrx.uncheckedCast(callbackBase);

            Scanner scanner = new Scanner(System.in);
            //String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();
            String username = getUsername(callbackPrx, server);

            while (true) {
                menu();
                System.out.println("Enter a command:");
                String input = scanner.nextLine();

                if (input.equals("exit")) {
                    Response response = server.executeCommand(username, "generate_report", null);
                    System.out.println(response.value);
                    server.leave(username);
                    break;
                } else if (input.equals("requeststest")) {
                    requeststest(server);
                    continue;
                }

                if (input.startsWith("list clients")) {
                    System.out.println(server.listUsernames());
                }else if (input.startsWith("to ")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length == 3) {
                        String[] splitMessage = input.split(":", 2);
                        String userHost = splitMessage[0];
                        String message = splitMessage[1];
                        String[] splitUsername = userHost.split(" ", 2);
                        String receptor = splitUsername[1];
                        server.sendMessage(username, message, receptor);
                    } else {
                        System.out.println("Usage: to <username> <message>");
                    }
                } else if (input.startsWith("BC:")) {
                    String[] parts = input.split(":", 2);
                    String message = parts[1];
                    server.broadcastMessage(username, message);
                } else {
                    System.out.println("hola mundo");
//                    totalRequests.incrementAndGet();
//                    try {
//                        String message = username + "@" + hostname + ":" + input;
//                        long time = System.currentTimeMillis();
//                        server.printString(message, callback, new HashMap<>());
//                        successfulRequests.incrementAndGet();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        failedRequests.incrementAndGet();
//                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void requeststest(PrinterPrx server) throws UnknownHostException {
        long time = System.currentTimeMillis();
        int throughput = 0;
        int unprocessed = 0;
        int total = 0;
        int missing = 0;
        String username = System.getProperty("user.name");
        String hostname = InetAddress.getLocalHost().getHostName();
//        while (System.currentTimeMillis() - time < 1000) {
//            String message = username + "@" + hostname + ":" + "listifs";
//            server.printString(message, null, new HashMap<>());
//            throughput++;
//            total = throughput + unprocessed + missing;
//        }
        System.out.println(" ");
        System.out.println("Tiempos para ejecucion de listifs");
        System.out.println("Throughput: " + throughput + " requests/s");
        System.out.println("Unprocessed: " + unprocessed + " requests/s");
        System.out.println("Missing: " + missing + " requests/s");
        System.out.println("Total: " + total + " requests/s");

        time = System.currentTimeMillis();
        throughput = 0;
        unprocessed = 0;
        missing = 0;
//        while (System.currentTimeMillis() - time < 1000) {
//            String message = username + "@" + hostname + ":" + "10";
//            server.printString(message, null, new HashMap<>());
//            throughput++;
//            total = throughput + unprocessed + missing;
//        }
        System.out.println(" ");
        System.out.println("Tiempos para ejecucion de fibonacci");
        System.out.println("Throughput: " + throughput + " requests/s");
        System.out.println("Unprocessed: " + unprocessed + " requests/s");
        System.out.println("Missing: " + missing + " requests/s");
        System.out.println("Total: " + total + " requests/s");

        time = System.currentTimeMillis();
        throughput = 0;
        unprocessed = 0;
        missing = 0;
//        while (System.currentTimeMillis() - time < 10000) {
//            String message = username + "@" + hostname + ":" + "listports localhost";
//            server.printString(message, null, new HashMap<>());
//            throughput++;
//            total = throughput + unprocessed + missing;
//        }
        System.out.println(" ");
        System.out.println("Tiempos para ejecucion de nmap");
        System.out.println("Throughput: " + throughput + " requests/s");
        System.out.println("Unprocessed: " + unprocessed + " requests/s");
        System.out.println("Missing: " + missing + " requests/s");
        System.out.println("Total: " + total + " requests/s");

        time = System.currentTimeMillis();
        throughput = 0;
        unprocessed = 0;
        missing = 0;
//        while (System.currentTimeMillis() - time < 1000) {
//            String message = username + "@" + hostname + ":" + "!java -version";
//            server.printString(message, null, new HashMap<>());
//            throughput++;
//        }
        System.out.println(" ");
        System.out.println("Tiempos para ejecucion de comando en consola");
        total = throughput + unprocessed + missing;
        System.out.println("Throughput: " + throughput + " requests/s");
        System.out.println("Unprocessed: " + unprocessed + " requests/s");
        System.out.println("Missing: " + missing + " requests/s");
        System.out.println("Total: " + total + " requests/s");
    }

    private static void menu() {
        System.out.println("Welcome to the chat room!");
        System.out.println("Type 'list clients' to see registered clients");
        System.out.println("Type 'to X: <message>' to send a message to client X");
        System.out.println("Type 'BC <message>' to broadcast a message");
        System.out.println("Type 'listifs' for network interfaces");
        System.out.println("Type 'listports <ip address>' for open ports on <ip address>");
        System.out.println("Type '!command' to execute command on server linux console");
        System.out.println("Type 'exit' to quit");
    }

    private static String getUsername(CallbackPrx callback, PrinterPrx server) {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (true) {
            System.out.println("Type username: ");
            input = scanner.nextLine();
            String result = server.join(input, callback);
            if(result.startsWith("User added:")){
                System.out.println(result);
                break;
            }
            System.out.println(result + ". Try again!");
        }
        return input;
    }
}