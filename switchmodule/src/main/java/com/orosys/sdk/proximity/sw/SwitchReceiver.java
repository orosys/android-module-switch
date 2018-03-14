package com.orosys.sdk.proximity.sw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orosys.sdk.proximity.sw.util.SwitchFileUtil;
import com.orosys.sdk.proximity.sw.util.SwitchPreference;
import com.orosys.sdk.proximity.sw.util.SwitchUtil;

/**
 * Created by oro on 2017. 2. 13..
 */

public class SwitchReceiver extends BroadcastReceiver {
    private static final String TAG = SwitchReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        Log.i(TAG, intent.getAction());
        try {
            onHandle(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onHandle(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            String lastWatchDog = SwitchPreference.getInstance(context).get(SwitchConstant.Key.LAST_WATCH_DOG);
            if (!SwitchUtil.isValidString(lastWatchDog)) {
                return;
            }

            String settings[] = lastWatchDog.split(",");
            if (settings.length < 3) {
                return;
            }

            try {
                String targetPackageName = settings[0];
                long period = Long.parseLong(settings[1]);
                String type = settings[2];
                SwitchUtil.setMonitorSDK(context, targetPackageName, period, type);
            } catch (NumberFormatException e) {
            }

        } else if (SwitchConstant.Action.REQUEST_SDK_INFO.equals(intent.getAction())) {
            SwitchInfo info = SwitchUtil.getCurrentSDKInfo(context);
            boolean devPositionPermission = SwitchUtil.checkPositionPermission(context);
            boolean infPositionPermission = info.getPositionPermission() == 1;
            if (devPositionPermission != infPositionPermission) {
                info.merged(new SwitchInfo.Builder().setPositionPermission(devPositionPermission ? 1 : 0).build());
                SwitchFileUtil.saveInfo(context, info);
            }
            try {
                Intent returnIntent = new Intent();
                returnIntent.setAction(SwitchConstant.Action.RESPONSE_SDK_INFO);
                returnIntent.setClassName(intent.getStringExtra(SwitchConstant.IntentName.PACKAGE), SwitchReceiver.class.getName());
                returnIntent.putExtra(SwitchConstant.IntentName.SWITCH_INFO, info);
                returnIntent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE));
                returnIntent.putExtra(SwitchConstant.IntentName.BROADCAST_COUNT, intent.getIntExtra(SwitchConstant.IntentName.BROADCAST_COUNT, 0));
                context.sendBroadcast(returnIntent);
            } catch (Exception e) {
            }

        } else if (SwitchConstant.Action.REQUEST_SDK_ENABLE.equals(intent.getAction())) {
            SwitchInfo info = SwitchUtil.getCurrentSDKInfo(context);

            // 단말 위치 상태 반영
            boolean devPositionPermission = SwitchUtil.checkPositionPermission(context);
            boolean infPositionPermission = info.getPositionPermission() == 1;
            if (devPositionPermission != infPositionPermission) {
                info.merged(new SwitchInfo.Builder().setPositionPermission(devPositionPermission ? 1 : 0).build());
                SwitchFileUtil.saveInfo(context, info);
            }

            // TODO : Proximity SDK enable check.

            try {
                Intent returnIntent = new Intent();
                returnIntent.setAction(SwitchConstant.Action.RESPONSE_SDK_ENABLE);
                returnIntent.setClassName(intent.getStringExtra(SwitchConstant.IntentName.PACKAGE), SwitchReceiver.class.getName());
                returnIntent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE));
                returnIntent.putExtra(SwitchConstant.IntentName.SWITCH_INFO, info);
                context.sendBroadcast(returnIntent);
            } catch (Exception e) {
            }

        } else if (SwitchConstant.Action.RESPONSE_SDK_ENABLE.equals(intent.getAction())) {
            Intent alarmIntent = new Intent(context, SwitchReceiver.class);
            alarmIntent.setAction(SwitchConstant.Action.COMPLETE_WATCH_DOG);
            SwitchUtil.cancelAlarm(context, alarmIntent);
            SwitchInfo switchInfo = SwitchUtil.getCurrentSDKInfo(context);

            SwitchConfig config = (SwitchConfig) SwitchPreference.getInstance(context).getParcelable(SwitchConfig.class, SwitchConstant.Key.SWITCH_CONFIG);
            SwitchInfo remoteSwitchInfo = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_INFO);
            String requestValue = intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE);
            long validTime = System.currentTimeMillis() - config.getSdkLivePeriod();

            boolean isEnable = true;
            if (remoteSwitchInfo == null) {
                isEnable = false;
                remoteSwitchInfo = SwitchInfo.Init();
            }
            if (switchInfo.getGroupId() != remoteSwitchInfo.getGroupId()) {
                isEnable = false;
            }

            if (SwitchConstant.Value.MASTER.equals(requestValue)) {
                // 최근 SDK 실행 시간 비교
                // TODO : 상용에서는 주석 해제
                /*if (switchInfo.getLastSDKRunTime() < validTime) {
                    isMasterEnable = false;
                    Log.i(TAG, "MASTER Time out");
                }*/
                if (!remoteSwitchInfo.isMasterEnable()) {
                    isEnable = false;
                    Log.i(TAG, "MASTER is disable");
                }

            } else if (SwitchConstant.Value.SLAVE.equals(requestValue)) {

            }

            if (!isEnable && SwitchConstant.Value.MASTER.equals(requestValue)) {
                // Master 복구
                Intent makeMasterIntent = new Intent(context, SwitchService.class);
                makeMasterIntent.setAction(SwitchConstant.Action.MAKE_MASTER);
                context.startService(makeMasterIntent);

            } else if (!isEnable && SwitchConstant.Value.SLAVE.equals(requestValue)) {
                // Slave 복구
                Intent makeSlaveIntent = new Intent(context, SwitchService.class);
                makeSlaveIntent.setAction(SwitchConstant.Action.MAKE_SLAVE);
                context.startService(makeSlaveIntent);
            }

        } else if (SwitchConstant.Action.WATCH_DOG.equals(intent.getAction())) {
            if (!isEnable(SwitchUtil.getCurrentSDKInfo(context))) {
                return;
            }
            String targetPackageName = intent.getStringExtra(SwitchConstant.IntentName.PACKAGE);
            long period = intent.getLongExtra(SwitchConstant.IntentName.PERIOD, -1);
            if (targetPackageName == null || period == -1) {
                return;
            }
            String type = intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE);
            SwitchUtil.setMonitorSDK(context, targetPackageName, period, type);

            Log.i(TAG, "WATCH_DOG type " + type + " monitor " + context.getPackageName() + " -> " + targetPackageName);

            try {
                Intent watchDogIntent = new Intent();
                watchDogIntent.setAction(SwitchConstant.Action.REQUEST_SDK_ENABLE);
                watchDogIntent.setClassName(targetPackageName, SwitchReceiver.class.getName());
                watchDogIntent.putExtra(SwitchConstant.IntentName.PACKAGE, context.getPackageName());
                watchDogIntent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, type);
                context.sendBroadcast(watchDogIntent);
            } catch (Exception e) {
            }

            Intent alarmIntent = new Intent(context, SwitchReceiver.class);
            alarmIntent.setAction(SwitchConstant.Action.COMPLETE_WATCH_DOG);
            alarmIntent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, type);

            SwitchConfig config = (SwitchConfig) SwitchPreference.getInstance(context).getParcelable(SwitchConfig.class, SwitchConstant.Key.SWITCH_CONFIG);
            if (config != null) {
                SwitchUtil.setAlarm(context, alarmIntent, System.currentTimeMillis() + config.getMonitorReportTime());
            } else {
                SwitchUtil.setAlarm(context, alarmIntent, System.currentTimeMillis() + SwitchConstant.DefaultPreference.MONITOR_REPORT);
            }

        } else if (SwitchConstant.Action.COMPLETE_WATCH_DOG.equals(intent.getAction())) {
            // SDK 가 응답하지 않음
            String type = intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE);
            if (SwitchConstant.Value.MASTER.equals(type)) {
                // Master 복구
                Intent makeMasterIntent = new Intent(context, SwitchService.class);
                makeMasterIntent.setAction(SwitchConstant.Action.MAKE_MASTER);
                context.startService(makeMasterIntent);

            } else if (SwitchConstant.Value.SLAVE.equals(type)) {
                // Slave 복구
                Intent makeMasterIntent = new Intent(context, SwitchService.class);
                makeMasterIntent.setAction(SwitchConstant.Action.MAKE_SLAVE);
                context.startService(makeMasterIntent);
            }

        } else {
            intent.setClass(context, SwitchService.class);
            context.startService(intent);
        }
    }

    private boolean isEnable(SwitchInfo info) {
        if (info.getSwitchEnable() > 0) {
            return true;
        }
        return false;
    }
}
