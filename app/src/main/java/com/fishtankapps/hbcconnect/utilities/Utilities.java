package com.fishtankapps.hbcconnect.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import java.net.InetAddress;

public final class Utilities {

    private Utilities(){}

    public static int getColor(Context context, int id){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, true);

        return typedValue.data;
    }

    public static int changeColorBrightness(int color, float factor){
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r,255), Math.min(g,255), Math.min(b,255));
    }


    public static boolean isInDarkMode(Context context){
        return (context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private static int isInternetAvailable = 0;
    public static synchronized boolean isInternetUnavailable() {
        new Thread(()->{
            isInternetAvailable = 0;
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                isInternetAvailable = (!ipAddr.toString().equals("")) ? 1 : -1;
            } catch (Exception e) {
                e.printStackTrace();
                isInternetAvailable = -1;
            }
        }).start();

        while(isInternetAvailable == 0)
            try{Thread.sleep(100);}catch (Exception ignore){}

        return isInternetAvailable != 1;
    }
}
