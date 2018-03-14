package com.orosys.sdk.proximity.sw.util;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.orosys.sdk.proximity.sw.SwitchConstant;
import com.orosys.sdk.proximity.sw.SwitchInfo;
import com.orosys.sdk.proximity.sw.SwitchReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oro on 2017. 2. 13..
 */

public class SwitchUtil {
    private static final String TAG = SwitchUtil.class.getSimpleName();

    public static List<String> findProximitySDKPackage(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<String> packageList = new ArrayList<>();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_SERVICES)) {
            if (packageInfo == null || packageInfo.services == null) {
                continue;
            }
            boolean isIncludedProximitySDK = false;
            for (ServiceInfo serviceInfo : packageInfo.services) {
                if (serviceInfo.name != null && serviceInfo.name.startsWith(SwitchConstant.CONSTANT)) {
                    isIncludedProximitySDK = true;
                }
            }
            if (isIncludedProximitySDK) {
                packageList.add(packageInfo.applicationInfo.packageName);
            }
        }
        return packageList;
    }

    /**
     * Get Proximity SDK Info
     *
     * @param context
     * @return List<SwitchInfo>
     */
    public static List<SwitchInfo> getProximitySDKInfo(Context context) {
        List<String> sdkList = findProximitySDKPackage(context);
        boolean externalStoragePermissionCheck = checkExternalStoragePermission(context);
        if (externalStoragePermissionCheck) {
            return SwitchFileUtil.getSDKInfoFromExternalStorage(context, sdkList);
        }
        return null;
    }

    public static SwitchInfo getCurrentSDKInfo(Context context) {
        return SwitchFileUtil.getInfo(SwitchFileUtil.getInfoFile(context));
    }

    public static boolean checkExternalStoragePermission(Context context) {
        // android m
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean checkPositionPermission(Context context) {
        // android m
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean isValidString(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }

        return true;
    }

    public static void setAlarm(Context context, Intent intent, long time) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        PendingIntent pending = PendingIntent.getBroadcast(context, intent.getAction().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        /*if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time + 1, pending);
        } else*/
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time + 1, pending);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time + 1, pending);
        }
    }

    public static void cancelAlarm(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        PendingIntent pending = PendingIntent.getBroadcast(context, intent.getAction().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pending);
    }

    public static void setMonitorSDK(Context context, String targetPackageName, long period, String type) {
        Intent intent = new Intent(context, SwitchReceiver.class);
        intent.setAction(SwitchConstant.Action.WATCH_DOG);
        intent.putExtra(SwitchConstant.IntentName.PACKAGE, targetPackageName);
        intent.putExtra(SwitchConstant.IntentName.PERIOD, period);
        intent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, type);
        setAlarm(context, intent, System.currentTimeMillis() + period);

        Log.i(TAG, "setMonitorSDK " + context.getPackageName() + "->" + targetPackageName + "," + period + "," + type);
        SwitchPreference.getInstance(context).save(SwitchConstant.Key.LAST_WATCH_DOG, targetPackageName + "," + period + "," + type);
    }

    public static void cancelMonitorSDK(Context context) {
        Intent intent = new Intent(context, SwitchReceiver.class);
        intent.setAction(SwitchConstant.Action.WATCH_DOG);
        cancelAlarm(context, intent);

        SwitchPreference.getInstance(context).save(SwitchConstant.Key.LAST_WATCH_DOG, "");
    }

    public static int getHightCommonLayerVersion(List<SwitchInfo> sdkList) {
        int highCommonLayerVersion = 0;
        if (sdkList == null) {
            return highCommonLayerVersion;
        }

        for (SwitchInfo switchInfo : sdkList) {
            if (!switchInfo.isMasterEnable()) {
                continue;
            }
            if (switchInfo.getCommonLayerVersion() > highCommonLayerVersion) {
                highCommonLayerVersion = switchInfo.getCommonLayerVersion();
            }
        }
        return highCommonLayerVersion;
    }
}
