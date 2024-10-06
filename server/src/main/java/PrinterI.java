import Demo.Response;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrinterI implements Demo.Printer {


    private List<ObjectPrx> clients = new ArrayList<>(); // Lista para almacenar los clientes

    @Override
    public Response printString(String message, Current __current) {
        // Crear un nuevo hilo para manejar la solicitud
        new Thread(() -> handleRequest(message, __current)).start();
        return new Response(0, "Request is being processed."); // Mensaje de respuesta inicial
    }


    /**
     * Process a message from the client.
     *
     * The message format is "user@host:command", where command is a string
     * that can be one of the following:
     *
     * 1. A number, in which case the server will return the Fibonacci
     * series up to the given number, along with the prime factors of the
     * number.
     * 2. "listifs", in which case the server will return a list of all
     * network interfaces on the server.
     * 3. "listports <ip address>", in which case the server will return a
     * list of open ports on the given IP address.
     * 4. "!command", in which case the server will execute the given
     * command on the server and return the output.
     * 5. Any other string, in which case the server will return "Unknown
     * command.".
     *
     * @param message the message from the client
     * @param __current the ICE Current object
     * @return a Response object containing the result of the command
     */

    public Response handleRequest(String message, Current __current) {
        String result = "";
        long time=0;
        try {
            String[] splitMessage = message.split(":", 2);
            String userHost = splitMessage[0];
            String command = splitMessage[1];


            // Registro de clientes
            if (command.startsWith("register")) {
                ObjectPrx clientProxy = (ObjectPrx) __current;
                String clientHost = command.split(" ")[1];

                if (!clients.contains(clientProxy)) {
                    clients.add(clientProxy);
                    result = "Client registered: " + clientHost;
                } else {
                    result = "Client already registered.";
                }
                return new Response(0, result);

            } else if (command.startsWith("list clients")) {

                result = String.join(", ", clients.toString());
                return new Response(0, result);

            } else if (command.startsWith("BC")) {
                String broadcastMessage = command.substring(3);
                result = "Broadcast message: " + broadcastMessage;
                for (ObjectPrx client : clients) {
                    try {
                        Demo.PrinterPrx clientPrinter = Demo.PrinterPrx.checkedCast(client);
                        if (clientPrinter != null) {
                            clientPrinter.printString("Broadcast from server: " + broadcastMessage);
                        }
                    } catch (Exception e) {
                        System.err.println("Error sending to client: " + e.getMessage());
                    }
                }
                return new Response(0, result);

            }else if (command.startsWith("to ")) {
                String[] parts = command.split(" ", 3);
                if (parts.length < 3) {
                    return new Response(0, "Error: Insufficient parameters for 'to' command.");
                }
                String recipient = parts[1];
                String messageToSend = parts[2];

                // Buscar el cliente en la lista
                for (ObjectPrx client : clients) {
                    Demo.PrinterPrx clientPrinter = Demo.PrinterPrx.checkedCast(client);
                    if (clientPrinter != null) {
                        String clientHost = clientPrinter.ice_getIdentity().name; // Obtener el nombre del cliente
                        String clientCategory = clientPrinter.ice_getIdentity().category; // Obtener la categoría del cliente
                        String clientIdentifier = clientHost + "@" + clientCategory; // Formar una identificación única

                        if (clientIdentifier.equals(recipient)) {
                            clientPrinter.printString("Message from " + userHost + ": " + messageToSend);
                            return new Response(0, "Message sent to " + recipient);
                        }
                    }
                }
                return new Response(0, "Error: Client " + recipient + " not found.");
            }
            else if (command.matches("\\d+")) {
                int n = Integer.parseInt(command);
                time = System.currentTimeMillis();
                String fibonacciSeries = Server.fibonacci(n);
                String primeFactors = Server.primeFactors(n);
                System.out.println(userHost + ": Fibonacci series up to " + n + " is: " + fibonacciSeries);
                result = fibonacciSeries + " - Prime factors: " + primeFactors;
            } else if (command.startsWith("listifs")) {
                time = System.currentTimeMillis();
                String interfaces = Server.listInterfaces();
                System.out.println(userHost + ": Network interfaces: " + interfaces);
                result = interfaces;
            } else if (command.startsWith("listports")) {
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    String ipAddress = parts[1];
                    time = System.currentTimeMillis();
                    result = Server.listOpenPorts(ipAddress);
                    System.out.println(userHost + ": Open ports for " + ipAddress + ": " + result);
                } else {
                    result = "Error: No IP address provided.";
                }
            } else if (command.startsWith("!")) {
                String cmd = command.substring(1);
                time = System.currentTimeMillis();
                result = Server.executeCommand(cmd);
                System.out.println(userHost + ": Command execution result: " + result);
            } else {
                result = "Unknown command.";
                System.out.println(userHost + ": " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error processing message.";
            return new Response(0, result);
        }
        if (time == 0) {
            return new Response(0, result);
        }
        long timetotal = System.currentTimeMillis() - time;
        return new Response(timetotal, result);
    }
}