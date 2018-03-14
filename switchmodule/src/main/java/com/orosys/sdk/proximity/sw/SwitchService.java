package com.orosys.sdk.proximity.sw;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.orosys.sdk.proximity.sw.util.SwitchFileUtil;
import com.orosys.sdk.proximity.sw.util.SwitchMemory;
import com.orosys.sdk.proximity.sw.util.SwitchPreference;
import com.orosys.sdk.proximity.sw.util.SwitchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by oro on 2017. 2. 13..
 */

public class SwitchService extends IntentService {
    private static final String TAG = SwitchService.class.getSimpleName();
    private SwitchConfig config = null;
    private SwitchInfo info = null;

    public SwitchService() {
        super("SwitchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        config = (SwitchConfig) SwitchPreference.getInstance(this).getParcelable(SwitchConfig.class, SwitchConstant.Key.SWITCH_CONFIG);
        if (config == null) {
            config = SwitchConfig.init();
            SwitchPreference.getInstance(this).save(SwitchConstant.Key.SWITCH_CONFIG, config);
        }
        info = SwitchUtil.getCurrentSDKInfo(this);

        boolean devPositionPermission = SwitchUtil.checkPositionPermission(this);
        boolean infPositionPermission = info.getPositionPermission() == 1;
        if (devPositionPermission != infPositionPermission) {
            info.merged(new SwitchInfo.Builder().setPositionPermission(devPositionPermission ? 1 : 0).build());
            SwitchFileUtil.saveInfo(this, info);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        Log.i(TAG, intent.getAction());
        try {
            onHandle(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onHandle(Intent intent) {
        if (SwitchConstant.Action.LAUNCH_APP.equals(intent.getAction())) {
            onAppLaunch(intent);
        } else if (SwitchConstant.Action.RUN_SDK.equals(intent.getAction())) {
            onSDKRunning(intent);
        } else if (SwitchConstant.Action.RESPONSE_SDK_INFO.equals(intent.getAction())) {
            onResponseSDKInfo(intent);
        } else if (SwitchConstant.Action.COMPLETE_RESPONSE_SDK_INFO.equals(intent.getAction())) {
            onCompleteResponseSDKInfo(intent);
        } else if (SwitchConstant.Action.UPDATE_INFO.equals(intent.getAction())) {
            onUpdateInfo(intent);
        } else if (SwitchConstant.Action.UPDATE_CONFIG.equals(intent.getAction())) {
            onUpdateConfig(intent);
        } else if (SwitchConstant.Action.MAKE_MASTER.equals(intent.getAction())) {
            onMakeMaster(intent);
        } else if (SwitchConstant.Action.MAKE_SLAVE.equals(intent.getAction())) {
            onMakeSlave(intent);
        } else if (SwitchConstant.Action.SET_MASTER.equals(intent.getAction())) {
            boolean success = onSetMaster(intent);
            if (!success) {
                // TODO : master setting failure
            }
        } else if (SwitchConstant.Action.SET_SLAVE.equals(intent.getAction())) {
            boolean success = onSetSlave(intent);
            if (!success) {
                // TODO : slave setting failure
            }
        } else if (SwitchConstant.Action.SET_NONE.equals(intent.getAction())) {
            boolean success = onSetNone(intent);
            if (!success) {
                // TODO : none setting failure
            }
        }
    }

    private void onUpdateConfig(Intent intent) {
        SwitchConfig switchConfig = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_CONFIG);
        onUpdateConfig(switchConfig);
    }

    private void onUpdateConfig(SwitchConfig switchConfig) {
        if (switchConfig == null) {
            return;
        }

        if (config != null) {
            config.merged(switchConfig);
        } else {
            config = switchConfig;
        }
        SwitchPreference.getInstance(this).save(SwitchConstant.Key.SWITCH_CONFIG, config);
    }

    private void onUpdateInfo(Intent intent) {
        SwitchInfo switchInfo = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_INFO);
        if (switchInfo == null) {
            return;
        }
        info.merged(switchInfo);

        boolean devPositionPermission = SwitchUtil.checkPositionPermission(this);
        boolean infPositionPermission = info.getPositionPermission() == 1;
        if (devPositionPermission != infPositionPermission) {
            info.merged(new SwitchInfo.Builder().setPositionPermission(devPositionPermission ? 1 : 0).build());
        }
        SwitchFileUtil.saveInfo(this, info);
    }

    private void onCompleteResponseSDKInfo(Intent intent) {
        List<SwitchInfo> sdkList = (List<SwitchInfo>) SwitchMemory.getInstance().get(SwitchConstant.Key.SDK_LIST);
        SwitchMemory.getInstance().put(SwitchConstant.Key.SDK_LIST, null);

        if (sdkList == null || sdkList.size() == 0) {
            sdkList = new ArrayList<>();
        }

        sdkList.add(info);

        String type = intent.getStringExtra(SwitchConstant.IntentName.REQUEST_VALUE);
        if (SwitchConstant.Value.MASTER.equals(type)) {
            linkMaster(sdkList);
        } else if (SwitchConstant.Value.SLAVE.equals(type)) {
            linkSlaveAndOther(sdkList);
        }
    }

    private void onResponseSDKInfo(Intent intent) {
        int broadcastCount = -1;
        SwitchInfo switchInfo = null;
        try {
            broadcastCount = intent.getIntExtra(SwitchConstant.IntentName.BROADCAST_COUNT, -1);
            switchInfo = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_INFO);
        } catch (Exception e) {
        }
        List<SwitchInfo> sdkList = (List<SwitchInfo>) SwitchMemory.getInstance().get(SwitchConstant.Key.SDK_LIST);
        if (switchInfo != null) {
            if (sdkList == null) {
                sdkList = new ArrayList<>();
            }
            sdkList.add(switchInfo);
            SwitchMemory.getInstance().put(SwitchConstant.Key.SDK_LIST, sdkList);
        }

        if (sdkList != null && sdkList.size() == broadcastCount) {
            Intent alarmIntent = new Intent(this, SwitchReceiver.class);
            alarmIntent.setAction(SwitchConstant.Action.COMPLETE_RESPONSE_SDK_INFO);
            SwitchUtil.cancelAlarm(this, alarmIntent);

            onCompleteResponseSDKInfo(intent);
        }
    }

    private void onAppLaunch(Intent intent) {
        info.merged(new SwitchInfo.Builder()
                .setPackageName(getPackageName())
                .setLastLaunchTime(System.currentTimeMillis())
                .build());
        SwitchFileUtil.saveInfo(this, info);
        onMakeMaster(intent);
    }

    private void onSDKRunning(Intent intent) {
        info.merged(new SwitchInfo.Builder()
                .setPackageName(getPackageName())
                .setLastSDKRunTime(System.currentTimeMillis())
                .build());
        SwitchFileUtil.saveInfo(this, info);
    }

    private boolean onSetMaster(Intent intent) {
        if (!info.isMasterEnable()) {
            SwitchUtil.setMonitorSDK(this, "none", config.getNoneMonitorMasterTime(), SwitchConstant.Value.MASTER);
            return false;
        }

        Long lastLaunchTime = (Long) SwitchMemory.getInstance().get(SwitchConstant.Key.LAST_LAUNCH_TIME);
        if (lastLaunchTime != null && System.currentTimeMillis() - lastLaunchTime < 1000) {
            return false;
        }

        SwitchMemory.getInstance().put(SwitchConstant.Key.LAST_LAUNCH_TIME, System.currentTimeMillis());

        onUpdateConfig(new SwitchConfig.Builder().setMasterPackageName(getPackageName()).build());
        info.merged(new SwitchInfo.Builder().setStatus(1).build());
        SwitchFileUtil.saveInfo(this, info);

        SwitchUtil.cancelMonitorSDK(this);

        /**
         * STEP:
         *  1. makeLink() 각 SDK 정보 수집
         *  2. linkSlaveAndOther(List<SwitchInfo> list) 수집완료 후 Master & Slave & None SDK 설정
         */
        makeLink(SwitchConstant.Value.SLAVE);

        startCommonSDK();

        Log.i(TAG, "I'm master~! " + info.toString());
        return true;
    }

    private boolean onSetSlave(Intent intent) {
        if (!info.isSlaveEnable()) {
            return false;
        }

        Intent alarmIntent = new Intent(this, SwitchReceiver.class);
        alarmIntent.setAction(SwitchConstant.Action.COMPLETE_RESPONSE_SDK_INFO);
        SwitchUtil.cancelAlarm(this, alarmIntent);

        // 마스터 감시
        String masterPackageName = intent.getStringExtra(SwitchConstant.IntentName.PACKAGE);
        if (SwitchUtil.isValidString(masterPackageName)) {
            SwitchUtil.setMonitorSDK(this, masterPackageName, config.getMonitorSDKTime(), SwitchConstant.Value.MASTER);
        }

        onUpdateConfig(intent);
        info.merged(new SwitchInfo.Builder().setStatus(2).build());
        SwitchFileUtil.saveInfo(this, info);

        stopCommonSDK();

        SwitchInfo masterInfo = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_INFO);
        Log.i(TAG, "I'm slave~! " + getPackageName() + " my master is " + masterInfo.toString());
        return true;
    }

    private boolean onSetNone(Intent intent) {
        Intent alarmIntent = new Intent(this, SwitchReceiver.class);
        alarmIntent.setAction(SwitchConstant.Action.COMPLETE_RESPONSE_SDK_INFO);
        SwitchUtil.cancelAlarm(this, alarmIntent);

        // 마스터 감시
        String masterPackageName = intent.getStringExtra(SwitchConstant.IntentName.PACKAGE);
        if (SwitchUtil.isValidString(masterPackageName)) {
            SwitchUtil.setMonitorSDK(this, masterPackageName, config.getNoneMonitorMasterTime(), SwitchConstant.Value.MASTER);
        }

        onUpdateConfig(intent);
        info.merged(new SwitchInfo.Builder().setStatus(3).build());
        SwitchFileUtil.saveInfo(this, info);

        stopCommonSDK();

        SwitchInfo masterInfo = intent.getParcelableExtra(SwitchConstant.IntentName.SWITCH_INFO);
        Log.i(TAG, "I'm none~! " + getPackageName() + " my master is " + masterInfo.toString());
        return true;
    }

    private void makeLink(String type) {
        List<SwitchInfo> sdkList = SwitchUtil.getProximitySDKInfo(this);
        if (sdkList != null && !SwitchConstant.Value.MASTER.equals(type)) {
            if (SwitchConstant.Value.MASTER.equals(type)) {
                linkMaster(sdkList);
            } else if (SwitchConstant.Value.SLAVE.equals(type)) {
                linkSlaveAndOther(sdkList);
            }

        } else {
            /**
             * SD 카드 권한이 없을 경우 || MASTER 인경우 Broadcast 를 이용하여 각 SDK 상태 파악
             * STEP:
             *  1. 단말에 설치된 SDK 들에 REQUEST_SDK_INFO 요청
             *  2. 수신받은 SDK 는 Receiver 에서 SwitchInfo 를 생성하여 전달
             *  3. RESPONSE_SDK_INFO 을 통해 각 SDK 로 부터 SwitchInfo 를 받음
             *  4. 현재 시간으로 부터 COMPLETE_RESPONSE_SDK_INFO 를 통하여 3초 뒤 받은 정보들을 저장
             *  5. linkSlaveAndOther(List<SwitchInfo> list) 수집완료 후 Slave & None SDK 설정
             */
            SwitchMemory.getInstance().put(SwitchConstant.Key.SDK_LIST, null);

            Intent intent = new Intent();
            intent.setAction(SwitchConstant.Action.REQUEST_SDK_INFO);
            intent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, type);
            sendSDKBroadcast(intent);

            Intent alarmIntent = new Intent(this, SwitchReceiver.class);
            alarmIntent.setAction(SwitchConstant.Action.COMPLETE_RESPONSE_SDK_INFO);
            alarmIntent.putExtra(SwitchConstant.IntentName.REQUEST_VALUE, type);

            SwitchUtil.setAlarm(this, alarmIntent, System.currentTimeMillis() + config.getMonitorReportTime());
        }
    }

    private void sendSDKBroadcast(Intent intent) {
        List<String> packageList = SwitchUtil.findProximitySDKPackage(this);
        String currentPackageName = getPackageName();
        for (String packageName : packageList) {
            if (currentPackageName.equals(packageName)) {
                continue;
            }
            intent.setClassName(packageName, SwitchReceiver.class.getName());
            intent.putExtra(SwitchConstant.IntentName.PACKAGE, getPackageName());
            intent.putExtra(SwitchConstant.IntentName.BROADCAST_COUNT, packageList.size() - 1);
            sendBroadcast(intent);
        }
    }

    private void linkMaster(List<SwitchInfo> sdkList) {
        updateByGroupId(sdkList);
        sortSDK(sdkList);
        SwitchInfo master = null;
        int highCommonLayerVersion = SwitchUtil.getHightCommonLayerVersion(sdkList);

        for (SwitchInfo switchInfo : sdkList) {
            if (!switchInfo.isMasterEnable()) {
                continue;
            }
            if (switchInfo.getCommonLayerVersion() < highCommonLayerVersion) {
                continue;
            }
            master = switchInfo;
            break;
        }

        if (master == null) {
            SwitchUtil.setMonitorSDK(this, "none", config.getNoneMonitorMasterTime(), SwitchConstant.Value.MASTER);
            return;
        }

        Intent intent = new Intent();
        intent.setAction(SwitchConstant.Action.SET_MASTER);
        intent.setClassName(master.getPackageName(), SwitchReceiver.class.getName());
        intent.putExtra(SwitchConstant.IntentName.PACKAGE, getPackageName());
        intent.putExtra(SwitchConstant.IntentName.SWITCH_INFO, info);
        sendBroadcast(intent);
    }

    private void linkSlaveAndOther(List<SwitchInfo> sdkList) {
        updateByGroupId(sdkList);
        sortSDK(sdkList);
        SwitchInfo slave = null;

        for (SwitchInfo switchInfo : sdkList) {
            if (switchInfo.getPackageName() == null || getPackageName().equals(switchInfo.getPackageName())) {
                continue;
            }
            Intent intent = new Intent();
            intent.setClassName(switchInfo.getPackageName(), SwitchReceiver.class.getName());
            intent.putExtra(SwitchConstant.IntentName.PACKAGE, getPackageName());
            intent.putExtra(SwitchConstant.IntentName.SWITCH_INFO, info);
            intent.putExtra(SwitchConstant.IntentName.SWITCH_CONFIG, config);

            if (slave == null && switchInfo.isSlaveEnable()) {
                slave = switchInfo;
                if (SwitchUtil.isValidString(slave.getPackageName())) {
                    SwitchUtil.setMonitorSDK(this, slave.getPackageName(), config.getMonitorSDKTime(), SwitchConstant.Value.SLAVE);
                }

                intent.setAction(SwitchConstant.Action.SET_SLAVE);
                sendBroadcast(intent);
            } else {
                intent.setAction(SwitchConstant.Action.SET_NONE);
                sendBroadcast(intent);
            }
        }

        if (slave == null) {
            SwitchUtil.setMonitorSDK(this, "none", config.getNoneMonitorMasterTime(), SwitchConstant.Value.SLAVE);
        }
    }

    private void updateByGroupId(List<SwitchInfo> sdkList) {
        int groupId = info.getGroupId();
        for (Iterator<SwitchInfo> it = sdkList.iterator(); it.hasNext(); ) {
            SwitchInfo value = it.next();
            if (value.getGroupId() != groupId) {
                it.remove();
            }
        }
    }

    private void sortSDK(List<SwitchInfo> sdkList) {
        // 최근 실행순으로 정렬
        Collections.sort(sdkList, new Comparator<SwitchInfo>() {
            @Override
            public int compare(SwitchInfo s1, SwitchInfo s2) {
                if (s1.getLastLaunchTime() == 0) {
                    return 1;
                }
                return s1.getLastLaunchTime() < s2.getLastLaunchTime() ? 1 : -1;
            }
        });
    }

    private void startCommonSDK() {
        // TODO : start sdk
    }

    private void stopCommonSDK() {
        // TODO : stop sdk
    }

    private void onMakeMaster(Intent intent) {
        makeLink(SwitchConstant.Value.MASTER);
    }

    private void onMakeSlave(Intent intent) {
        makeLink(SwitchConstant.Value.SLAVE);
    }
}
