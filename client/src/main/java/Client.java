import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

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
                }else {
                    Response response = server.executeCommand(username, input, null);
                    if (response != null) {
                        System.out.println(response.value);
                    } else {
                        System.out.println("Invalid command.");
                    }

                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void requeststest(PrinterPrx server) throws UnknownHostException {
        String username = System.getProperty("user.name");
        String hostname = InetAddress.getLocalHost().getHostName();

        // Definir la cantidad de repeticiones por comando
        int repetitions = 10000; // Número de repeticiones por segundo
        long startTime;
        Response reportResponse;

        // Ejecución del comando "listifs"
        startTime = System.currentTimeMillis();
        CompletableFuture<Void>[] listifsFutures = new CompletableFuture[repetitions];
        for (int i = 0; i < repetitions; i++) {
            listifsFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Response response = server.executeCommand(username, "listifs", null);
                    System.out.println("Response for listifs: " + response.value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        CompletableFuture<Void> allListifs = CompletableFuture.allOf(listifsFutures);
        allListifs.join();
        System.out.println("Executed " + repetitions + " 'listifs' commands in " + (System.currentTimeMillis() - startTime) + " ms.");

        // Generar reporte después de 'listifs'
        reportResponse = server.executeCommand(username, "generate_report", null);

        // Ejecución del comando "fibonacci"
        startTime = System.currentTimeMillis();
        CompletableFuture<Void>[] fibonacciFutures = new CompletableFuture[repetitions];
        for (int i = 0; i < repetitions; i++) {
            fibonacciFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Response response = server.executeCommand(username, "10", null); // Comando de Fibonacci
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        CompletableFuture<Void> allFibonacci = CompletableFuture.allOf(fibonacciFutures);
        allFibonacci.join();
        System.out.println("Executed " + repetitions + " 'fibonacci' commands in " + (System.currentTimeMillis() - startTime) + " ms.");

        // Generar reporte después de 'fibonacci'
        reportResponse = server.executeCommand(username, "generate_report", null);

        // Ejecución del comando "listports"
        startTime = System.currentTimeMillis();
        CompletableFuture<Void>[] listportsFutures = new CompletableFuture[repetitions];
        for (int i = 0; i < repetitions; i++) {
            listportsFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Response response = server.executeCommand(username, "listports localhost", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        CompletableFuture<Void> allListports = CompletableFuture.allOf(listportsFutures);
        allListports.join();
        System.out.println("Executed " + repetitions + " 'listports' commands in " + (System.currentTimeMillis() - startTime) + " ms.");
        reportResponse = server.executeCommand(username, "generate_report", null);
        System.out.println(reportResponse.value);

        // Ejecución de comandos personalizados
        startTime = System.currentTimeMillis();
        String customCommand = "!java -version"; // Ejemplo de comando personalizado
        CompletableFuture<Void>[] customCommandFutures = new CompletableFuture[repetitions];
        for (int i = 0; i < repetitions; i++) {
            customCommandFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Response response = server.executeCommand(username, customCommand, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        CompletableFuture<Void> allCustomCommands = CompletableFuture.allOf(customCommandFutures);
        allCustomCommands.join();
        System.out.println("Executed " + repetitions + " custom commands in " + (System.currentTimeMillis() - startTime) + " ms.");
        // Generar reporte final
        reportResponse = server.executeCommand(username, "generate_report", null);
        System.out.println(reportResponse.value);
    }

    private static void menu() {
        System.out.println("Welcome to the chat room!");
        System.out.println("Type 'list clients' to see registered clients");
        System.out.println("Type 'to X: <message>' to send a message to client X");
        System.out.println("Type 'BC <message>' to broadcast a message");
        System.out.println("Type 'listifs' for network interfaces");
        System.out.println("Type 'listports <ip address>' for open ports on <ip address>");
        System.out.println("Type '!command' to execute command on server linux console");
        System.out.println("Type a number to get the fibonacci series up to that number");
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