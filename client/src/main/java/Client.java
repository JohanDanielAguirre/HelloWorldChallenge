import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Client
{
    /**
     * The main entry point for the client.
     *
     * <p>This program prints out a message to the user, and then waits for
     * the user to enter a message. It sends the message to the server and
     * prints out the response. It then waits for the user to enter another
     * message, and so on. If the user enters "exit", the program will quit.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int status = 0;
        Communicator communicator = null;
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        AtomicInteger unprocessedRequests = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Array multidimensional para almacenar las métricas
        String[][] metricsArray = new String[5][2]; // 5 filas (número de métricas), 2 columnas (métrica y valor)

        try {
            communicator = Util.initialize(args, "config.client");
            ObjectPrx base = communicator.stringToProxy("SimpleServer:default -p 9099");
            PrinterPrx server = PrinterPrx.checkedCast(base);
            if (server == null) throw new Error("Invalid proxy");
            Scanner scanner = new Scanner(System.in);
            String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();
            server.printString("register " + hostname);

            while (true) {
                System.out.println("Welcome " + username + " on " + hostname + ".");
                System.out.print("Enter a command:\n" +
                        "'list clients' to see registered clients\n" +
                        "'to X: <message>' to send a message to client X\n" +
                        "'BC <message>' to broadcast a message\n" );
                System.out.print("Enter a number to get the Fibonacci series up to that number\n" +
                        "'listifs' for network interfaces\n" +
                        "'listports <ip address>' for open ports on <ip address>\n" +
                        "'!command' to execute command on server linux console\n" +
                        "or 'exit' to quit:\n");
                String input = scanner.nextLine();

                // Exit and print throughput
                if (input.equals("exit")) {
                    break;
                }else if (input.equals("requeststest")) {
                    requeststest(server);
                    continue;
                }
                totalRequests.incrementAndGet();
                try {
                    String message = username + "@" + hostname + ":" + input;
                    long time = System.currentTimeMillis();
                    Response response = server.printString(message);
                    System.out.println("Server response: " + response.value);
                    System.out.println("Time taken total: " + (System.currentTimeMillis() - time) + "ms");
                    System.out.println("Received response or processing time of " + response.responseTime + "ms");
                    System.out.println("latency is " + (System.currentTimeMillis() - time - response.responseTime) + "ms");

                    successfulRequests.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                    failedRequests.incrementAndGet();
                }
            }

            // Calcular los unprocessed
            unprocessedRequests.set(totalRequests.get() - successfulRequests.get() - failedRequests.get());

            // Calcular el throughput
            long totalTime = System.currentTimeMillis() - startTime;
            double throughput = totalRequests.get() / ((double) totalTime/100.0);

            // Almacenar los valores en el array multidimensional
            metricsArray[0][0] = "Total Requests";
            metricsArray[0][1] = String.valueOf(totalRequests.get());

            metricsArray[1][0] = "Successful Requests";
            metricsArray[1][1] = String.valueOf(successfulRequests.get());

            metricsArray[2][0] = "Failed Requests";
            metricsArray[2][1] = String.valueOf(failedRequests.get());

            metricsArray[3][0] = "Unprocessed Requests";
            metricsArray[3][1] = String.valueOf(unprocessedRequests.get());

            metricsArray[4][0] = "Throughput (requests/s)";
            metricsArray[4][1] = String.format("%.4f", throughput);

            // Imprimir la tabla con las métricas
            System.out.println("\n--- Metrics Summary ---");
            System.out.format("+------------------------+---------------------+%n");
            System.out.format("| Metric                 | Value               |%n");
            System.out.format("+------------------------+---------------------+%n");

            for (String[] row : metricsArray) {
                System.out.format("| %-22s | %-19s |%n", row[0], row[1]);
            }
            System.out.format("+------------------------+---------------------+%n");
        } catch (Exception e) {
            e.printStackTrace();
            status = 1;
        } finally {
            if (communicator != null) {
                communicator.destroy();
            }
            System.exit(status);
        }
    }

    /**
     * Test the performance of the server by sending a series of messages to the server
     * and then printing out the results.
     *
     * @param server the server to test
     * @throws UnknownHostException if the hostname for the server could not be found
     */
    private static void requeststest(PrinterPrx server) throws UnknownHostException {
        long time = System.currentTimeMillis();
        int throughput = 0;
        int unprocessed = 0;
        int total = 0;
        int missing = 0;
        String username = System.getProperty("user.name");
        String hostname = InetAddress.getLocalHost().getHostName();
        while (System.currentTimeMillis() - time < 1000) {
            String message = username + "@" + hostname + ":" + "listifs";
            Response response = (server.printString(message));
            if (response.responseTime > 0) {
                throughput++;
            } else {
                unprocessed++;
            }
            if (response.value == null || response.value.isEmpty()) {
                {
                    missing++;
                }
            }
            total = throughput + unprocessed + missing;
        }
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
        while (System.currentTimeMillis() - time < 1000) {
            String message = username + "@" + hostname + ":" + "10";
            Response response = (server.printString(message));
            if (response.responseTime > 0) {
                throughput++;
            } else {
                unprocessed++;
            }
            if (response.value == null || response.value.isEmpty()) {
                {
                    missing++;
                }
            }
            total = throughput + unprocessed + missing;
        }
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
        while (System.currentTimeMillis() - time < 10000) {
            String message = username + "@" + hostname + ":" + "listports localhost";
            Response response = (server.printString(message));
            if (response.responseTime > 0) {
                throughput++;
            } else {
                unprocessed++;
            }
            if (response.value == null || response.value.isEmpty()) {
                {
                    missing++;
                }
            }
            total = throughput + unprocessed + missing;
        }
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
        while (System.currentTimeMillis() - time < 1000) {
            String message = username + "@" + hostname + ":" + "!java -version";
            Response response = (server.printString(message));
            if (response.responseTime > 0) {
                throughput++;
            } else {
                unprocessed++;
            }
            if (response.value == null || response.value.isEmpty()) {
                {
                    missing++;
                }
            }
        }
        System.out.println(" ");
        System.out.println("Tiempos para ejecucion de comando en consola");
        total = throughput + unprocessed + missing;
        System.out.println("Throughput: " + throughput + " requests/s");
        System.out.println("Unprocessed: " + unprocessed + " requests/s");
        System.out.println("Missing: " + missing + " requests/s");
        System.out.println("Total: " + total + " requests/s");
    }
}