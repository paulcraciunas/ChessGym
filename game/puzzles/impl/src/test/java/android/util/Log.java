package android.util;

import androidx.annotation.NonNull;

/** @noinspection ALL*/
public class Log {
    public static int e(String tag, String msg, @NonNull Throwable error) {
        System.out.println("ERROR: " + tag + ": " + msg + ", " + error.toString());
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("WARNING: " + tag + ": " + msg);
        return 0;
    }
}
