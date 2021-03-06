package com.ahsailabs.alcore.constants;

import android.content.Context;
import android.text.TextUtils;

import com.ahsailabs.alcore.R;
import com.ahsailabs.alutils.CommonUtil;

/**
 * Created by ahsai on 3/15/2018.
 */

public class AlCoreConstanta {
    public static final String ACTION_MANAGE_SHAUM_SHOLAT_REMINDER = "com.zaitunlabs.zlcore.action.MANAGE_SHAUM_SHOLAT_REMINDER";
    public static final String TELEPHONE_SCHEMA = "tel:";
    public static final String MAILTO_SCHEMA = "mailto:";
    public static String DATABASE_NAME = "alcore.db";
    public static int DATABASE_VERSION = 1;
    public static String CRASH_MAIL_TO = null;

    public static void setDatabaseName(String databaseName) {
        DATABASE_NAME = databaseName;
    }

    public static void setDatabaseVersion(int databaseVersion) {
        DATABASE_VERSION = databaseVersion;
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    public static String getDatabaseName(Context context) {
        return (TextUtils.isEmpty(AlCoreConstanta.DATABASE_NAME)? CommonUtil.getPackageName(context): AlCoreConstanta.DATABASE_NAME)+".db";
    }

    public static void setCrashMailTo(String crashMailTo) {
        CRASH_MAIL_TO = crashMailTo;
    }

    public static String getCrashMailTo(Context context) {
        return (CRASH_MAIL_TO==null?context.getString(R.string.zlcore_crash_mail_to):CRASH_MAIL_TO);
    }
}
