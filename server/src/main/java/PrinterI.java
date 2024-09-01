import Demo.Response;
import com.zeroc.Ice.Current;

public class PrinterI implements Demo.Printer {
    @Override
    public Response printString(String message, Current __current) {
        String result = "";
        try {
            String[] splitMessage = message.split(":", 2);
            String userHost = splitMessage[0];
            String command = splitMessage[1];

            if (command.matches("\\d+")) {
                int n = Integer.parseInt(command);
                String fibonacciSeries = Server.fibonacci(n);
                String primeFactors = Server.primeFactors(n);
                System.out.println(userHost + ": Fibonacci series up to " + n + " is: " + fibonacciSeries);
                result = fibonacciSeries + " - Prime factors: " + primeFactors;
            } else if (command.startsWith("listifs")) {
                String interfaces = Server.listInterfaces();
                System.out.println(userHost + ": Network interfaces: " + interfaces);
                result = interfaces;
            } else if (command.startsWith("listports")) {
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    String ipAddress = parts[1];
                    result = Server.listOpenPorts(ipAddress);
                    System.out.println(userHost + ": Open ports for " + ipAddress + ": " + result);
                } else {
                    result = "Error: No IP address provided.";
                }
            } else if (command.startsWith("!")) {
                String cmd = command.substring(1);
                result = Server.executeCommand(cmd);
                System.out.println(userHost + ": Command execution result: " + result);
            } else {
                result = "Unknown command.";
                System.out.println(userHost + ": " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error processing message.";
        }
        return new Response(0, result);
    }
}