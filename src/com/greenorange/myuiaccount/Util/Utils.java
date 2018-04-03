package com.greenorange.myuiaccount.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.greenorange.myuiaccount.service.V2.Config;

/**
 * Created by JasWorkSpace on 15/10/22.
 */
public class Utils {

    public static Intent createFindPassWordIntent(Context context){
        return new Intent(Intent.ACTION_VIEW, Uri.parse(Config.getFindpsaawordUrl()));
    }
    public static Intent createUserChangeUserInfoIntent(Context context){
        return new Intent(Intent.ACTION_VIEW, Uri.parse(Config.getChangeuserinfoUrl()));
    }
    ///////////////////////////
    public static String getString(String string){
        return TextUtils.isEmpty(string) ? "" : string;
    }
}
