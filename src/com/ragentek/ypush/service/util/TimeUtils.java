package com.ragentek.ypush.service.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	public static String getTime(long time){
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
	public static String getCurrentTime(){
        return getTime(System.currentTimeMillis());
    }
    public static String getDayTime(long time){
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    public static String getCurrentDayTime(){
        return getDayTime(System.currentTimeMillis());
    }
}
