import Demo.Response;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class PrinterI implements Demo.Printer {
    public Response printString(String s, com.zeroc.Ice.Current current) {
        System.out.println(s);
        return new Response(0, "Server response: " + s);

       String clientInfo = current.con.toString(); // Asumiendo que esto obtiene la información del cliente
        String responseMessage = "";

        try {
            if (s.startsWith("listifs")) {
                List<String> interfaces = getNetworkInterfaces();
                System.out.println(clientInfo + ": " + interfaces);
                responseMessage = "Interfaces: " + interfaces;
            } else if (s.startsWith("listports")) {
                String ip = s.split(" ")[1];
                List<String> openPorts = getOpenPorts(ip);
                System.out.println(clientInfo + ": " + openPorts);
                responseMessage = "Open ports: " + openPorts;
            } else if (s.startsWith("!")) {
                String command = s.substring(1);
                String commandOutput = executeCommand(command);
                System.out.println(clientInfo + ": " + commandOutput);
                responseMessage = "Command output: " + commandOutput;
            } else {
                int n = Integer.parseInt(s);
                if (n > 0) {
                    List<Integer> fibonacciSeries = getFibonacciSeries(n);
                    List<Integer> primeFactors = getPrimeFactors(n);
                    System.out.println(clientInfo + ": " + fibonacciSeries);
                    responseMessage = "Prime factors: " + primeFactors;
                } else {
                    responseMessage = "Received non-positive integer.";
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(s);
            responseMessage = "Server response: " + s;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Integer> getFibonacciSeries(int n) {
        List<Integer> series = new ArrayList<>();
        int a = 0, b = 1;
        while (n-- > 0) {
            series.add(a);
            int sum = a + b;
            a = b;
            b = sum;
        }
        return series;
    }

    private List<Integer> getPrimeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors;
    }

    private List<String> getNetworkInterfaces() throws SocketException {
        return null;
    }

    private List<String> getOpenPorts(String ip) {
        return null;
    }

    private String executeCommand(String command) {
        return null;
    }
}