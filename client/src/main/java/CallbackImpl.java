import com.zeroc.Ice.Current;
import Demo.Response;
import Demo.Callback;

public class CallbackImpl implements Callback {

    @Override
    public void receiveMessage(String message, Current current) {
        System.out.println("Received message: " + message);
    }
}
