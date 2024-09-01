import Demo.Response;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class PrinterI implements Demo.Printer {
    public Response printString(String s, com.zeroc.Ice.Current current) {
        System.out.println(s);
        return new Response(0, "Server response: " + s);
    }
}