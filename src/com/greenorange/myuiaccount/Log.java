package com.greenorange.myuiaccount;

/**
 * Created by JasWorkSpace on 15/10/16.
 */
public class Log {
    private final static String  TAG = "yy";
    private final static boolean DEBUG = true;
    private final static int     NOLOG = -1;
    public static int  d(String message){
        return d(TAG, message);
    }
    public static int d(String TAG, String message){
        if(!DEBUG)return NOLOG;
        return android.util.Log.d(TAG, message);
    }
    public static int i(String message){
        return i(TAG, message);
    }
    public static int i(String tag, String message){
        return android.util.Log.i(tag, message);
    }
}
