package com.ahsailabs.alcore.utils;


import com.ahsailabs.alcore.BuildConfig;

/**
 * Created by ahmad s on 06/10/20.
 */
class VersionUtil {
    public static String getVersionName(){
        return BuildConfig.VERSION_NAME;
    }

    public static int getVersionCode(){
        return BuildConfig.VERSION_CODE;
    }
}
