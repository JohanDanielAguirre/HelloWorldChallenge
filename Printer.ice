module Demo
{
    class Response{
        long responseTime;
        string value;
    }

    interface Callback{
        void receiveMessage(string message);
    }

    interface Printer
    {
        string join(string username, Callback* callback);
        string listUsernames();
        void broadcastMessage(string sender, string s);
        void sendMessage(string sender, string s, string receptor);
        string leave(string username);
        Response executeCommand(string username, string command);
    }


}