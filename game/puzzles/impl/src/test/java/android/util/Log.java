package android.util;

public class Log {
    public static int e(String tag, String msg, Throwable error) {
        System.out.println("ERROR: " + tag + ": " + msg + ", " + error.toString());
        return 0;
    }
}
