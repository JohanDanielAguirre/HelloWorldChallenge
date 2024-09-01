import Demo.Response;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs)) {
            //com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
            Response response = null;
            Demo.PrinterPrx service = Demo.PrinterPrx
                    .checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            if (service == null) {
                throw new Error("Invalid proxy");
            }

            Scanner sc = new Scanner(System.in);
            String s = sc.nextLine();

            while (!s.equals("exit")) {
                //response = service.printString("Hello World from a remote client!");
                response = service.printString(s);
                System.out.println("Respuesta del server: " + response.value + ", " + response.responseTime);
                s = sc.nextLine();
            }

        }
    }
}