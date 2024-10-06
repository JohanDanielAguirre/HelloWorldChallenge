module Demo
{
    class Response{
        long responseTime;
        string value;
    }
    interface Printer
    {
        Response printString(string s);
        Response sendMessage(String message, Current __current);
    }
}