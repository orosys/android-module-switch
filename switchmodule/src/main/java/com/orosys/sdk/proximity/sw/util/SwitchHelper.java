package com.orosys.sdk.proximity.sw.util;

import android.content.Context;
import android.content.Intent;

import com.orosys.sdk.proximity.sw.SwitchConfig;
import com.orosys.sdk.proximity.sw.SwitchConstant;
import com.orosys.sdk.proximity.sw.SwitchInfo;
import com.orosys.sdk.proximity.sw.SwitchService;

/**
 * Created by oro on 2017. 2. 14..
 */

public class SwitchHelper {
    /**
     * Switch 구동 초기화
     *
     * @param context
     */
    public static void launchApp(Context context) {
        Intent intent = new Intent(context, SwitchService.class);
        intent.setAction(SwitchConstant.Action.LAUNCH_APP);
        context.startService(intent);
    }

    public static void runSDK(Context context) {
        Intent intent = new Intent(context, SwitchService.class);
        intent.setAction(SwitchConstant.Action.RUN_SDK);
        context.startService(intent);
    }

    public static void setConfig(Context context, SwitchConfig config) {
        Intent intent = new Intent(context, SwitchService.class);
        intent.setAction(SwitchConstant.Action.UPDATE_CONFIG);
        intent.putExtra(SwitchConstant.IntentName.SWITCH_CONFIG, config);
        context.startService(intent);
    }

    public static SwitchConfig getConfig(Context context) {
        return (SwitchConfig) SwitchPreference.getInstance(context).getParcelable(SwitchConfig.class, SwitchConstant.Key.SWITCH_CONFIG);
    }

    public static void setInfo(Context context, SwitchInfo info) {
        Intent intent = new Intent(context, SwitchService.class);
        intent.setAction(SwitchConstant.Action.UPDATE_INFO);
        intent.putExtra(SwitchConstant.IntentName.SWITCH_INFO, info);
        context.startService(intent);
    }

    public static SwitchInfo getInfo(Context context) {
        return SwitchUtil.getCurrentSDKInfo(context);
    }

    public static String getStatus(Context context) {
        int status = getInfo(context).getStatus();
        if (status == 1) {
            return SwitchConstant.Value.MASTER;
        } else if (status == 2) {
            return SwitchConstant.Value.SLAVE;
        } else if (status == 3) {
            return SwitchConstant.Value.NONE;
        }
        return "";
    }
}
