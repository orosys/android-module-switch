package com.orosys.sdk.proximity.sw;

/**
 * Created by oro on 2017. 2. 13..
 */

public class SwitchConstant {
    public static final String CONSTANT = "com.orosys.sdk.proximity.sw";

    public static class DefaultPreference {
        // Report 요청 후 결과를 확인하기 위한 시간 millisecond {Default 3second}
        public static final long MONITOR_REPORT = 3000;
        // Slave 가 master 감시 시간 millisecond {Default 1hour}
        public static final long MONITOR_SDK = 3600000;
        // None 가 master 감시 시간 millisecond {Default 12hour}
        public static final long NONE_MONITOR_MASTER = 43200000;
        // SDK 동작확인 가능한 최소 시간 millisecond {Default 1hour}
        public static final long SDK_LIVE_PERIOD = 3600000;
    }

    public static class Broadcast {
        public static final String RECEIVER = CONSTANT + ".RECEIVER";
    }

    public static class Action {
        private static final String ACTION = CONSTANT + ".action";

        /**
         * Master SDK 가 Slave SDK 에게 SwitchInfo object 요청
         */
        public static final String REQUEST_SDK_INFO = ACTION + ".REQUEST_SDK_INFO";
        /**
         * REQUEST_SDK_INFO 를 받은 후 SLAVE, NONE 설정
         */
        public static final String RESPONSE_SDK_INFO = ACTION + ".RESPONSE_SDK_INFO";
        /**
         * 여러 SDK 에서 받은 SwitchInfo 를 저장
         */
        public static final String COMPLETE_RESPONSE_SDK_INFO = ACTION + ".COMPLETE_RESPONSE_SDK_INFO";
        /**
         * SDK Enable 확인
         */
        public static final String REQUEST_SDK_ENABLE = ACTION + ".REQUEST_SDK_ENABLE";
        /**
         * SDK Enable 요청 수신
         */
        public static final String RESPONSE_SDK_ENABLE = ACTION + ".RESPONSE_SDK_ENABLE";
        /**
         * 앱 구동시 실행
         */
        public static final String LAUNCH_APP = ACTION + ".LAUNCH_APP";
        /**
         * Proximity SDK 구동시 실행
         */
        public static final String RUN_SDK = ACTION + ".RUN_SDK";
        /**
         * 각 시간 설정 업데이트
         */
        public static final String UPDATE_CONFIG = ACTION + ".UPDATE_CONFIG";
        /**
         * Position permission, user info agree 설정 업데이트
         */
        public static final String UPDATE_INFO = ACTION + ".UPDATE_INFO";
        /**
         * Master 수행
         */
        public static final String SET_MASTER = ACTION + ".SET_MASTER";
        /**
         * Slave 수행
         */
        public static final String SET_SLAVE = ACTION + ".SET_SLAVE";
        /**
         * None 수행
         */
        public static final String SET_NONE = ACTION + ".SET_NONE";
        /**
         * Watch dog alarm
         */
        public static final String WATCH_DOG = ACTION + ".WATCH_DOG";
        /**
         * Heartbeat check 완료 타이머
         */
        public static final String COMPLETE_WATCH_DOG = ACTION + ".COMPLETE_WATCH_DOG";
        /**
         * Master 복구
         */
        public static final String MAKE_MASTER = ACTION + ".MAKE_MASTER";
        /**
         * Slave 복구
         */
        public static final String MAKE_SLAVE = ACTION + ".MAKE_SLAVE";
    }

    public static class IntentName {
        private static final String INTENT_NAME = "name";
        public static final String PACKAGE = INTENT_NAME + ".PACKAGE";
        public static final String BROADCAST_COUNT = INTENT_NAME + ".BROADCAST_COUNT";
        public static final String REQUEST_VALUE = INTENT_NAME + ".REQUEST_VALUE";
        public static final String SWITCH_INFO = INTENT_NAME + ".SWITCH_INFO";
        public static final String SWITCH_CONFIG = INTENT_NAME + ".SWITCH_CONFIG";
        public static final String PERIOD = INTENT_NAME + ".PERIOD";
    }

    public static class Key {
        public static final String SDK_LIST = "sdk_list";
        public static final String LAST_WATCH_DOG = "last_watch_dog";
        public static final String LAST_LAUNCH_TIME = "last_launch_time";
        public static final String SWITCH_CONFIG = "switch_config";
    }

    public static class Value {
        public static final String MASTER = "master";
        public static final String SLAVE = "slave";
        public static final String NONE = "none";
    }
}
