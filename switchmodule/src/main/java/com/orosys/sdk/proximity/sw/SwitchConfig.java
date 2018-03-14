package com.orosys.sdk.proximity.sw;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by oro on 2017. 2. 14..
 */

public class SwitchConfig implements Parcelable {

    private long monitorSDKTime = -1;
    private long noneMonitorMasterTime = -1;
    private long monitorReportTime = -1;
    private long sdkLivePeriod = -1;
    private String masterPackageName = null;
    private String tag = null;

    private SwitchConfig() {

    }

    public static SwitchConfig init() {
        return new SwitchConfig.Builder()
                .setMonitorReportTime(SwitchConstant.DefaultPreference.MONITOR_REPORT)
                .setNoneMonitorMasterTime(SwitchConstant.DefaultPreference.NONE_MONITOR_MASTER)
                .setMonitorSDKTime(SwitchConstant.DefaultPreference.MONITOR_SDK)
                .setSDKLivePeriod(SwitchConstant.DefaultPreference.SDK_LIVE_PERIOD)
                .build();
    }

    protected SwitchConfig(Parcel in) {
        monitorSDKTime = in.readLong();
        noneMonitorMasterTime = in.readLong();
        monitorReportTime = in.readLong();
        sdkLivePeriod = in.readLong();
        masterPackageName = in.readString();
        tag = in.readString();
    }

    public static final Creator<SwitchConfig> CREATOR = new Creator<SwitchConfig>() {
        @Override
        public SwitchConfig createFromParcel(Parcel in) {
            return new SwitchConfig(in);
        }

        @Override
        public SwitchConfig[] newArray(int size) {
            return new SwitchConfig[size];
        }
    };

    public void merged(SwitchConfig config) {
        if (config.getMonitorReportTime() > -1) {
            monitorReportTime = config.getMonitorReportTime();
        }
        if (config.getNoneMonitorMasterTime() > -1) {
            noneMonitorMasterTime = config.getNoneMonitorMasterTime();
        }
        if (config.getSdkLivePeriod() > -1) {
            sdkLivePeriod = config.getSdkLivePeriod();
        }
        if (config.getMonitorSDKTime() > -1) {
            monitorSDKTime = config.getMonitorSDKTime();
        }
        if (config.getMasterPackageName() != null && config.getMasterPackageName().length() > 0) {
            masterPackageName = config.getMasterPackageName();
        }
        if (config.getTag() != null && config.getTag().length() > 0) {
            tag = config.getTag();
        }
    }

    public long getMonitorReportTime() {
        return monitorReportTime;
    }

    public long getNoneMonitorMasterTime() {
        return noneMonitorMasterTime;
    }

    public long getSdkLivePeriod() {
        return sdkLivePeriod;
    }

    public long getMonitorSDKTime() {
        return monitorSDKTime;
    }

    public String getMasterPackageName() {
        return masterPackageName;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(monitorSDKTime);
        parcel.writeLong(noneMonitorMasterTime);
        parcel.writeLong(monitorReportTime);
        parcel.writeLong(sdkLivePeriod);
        parcel.writeString(masterPackageName);
        parcel.writeString(tag);
    }

    public static class Builder {
        private SwitchConfig switchConfig;

        public Builder() {
            switchConfig = new SwitchConfig();
        }

        public SwitchConfig build() {
            return switchConfig;
        }

        public Builder setMonitorSDKTime(long millisecond) {
            switchConfig.monitorSDKTime = millisecond;
            return this;
        }

        public Builder setNoneMonitorMasterTime(long millisecond) {
            switchConfig.noneMonitorMasterTime = millisecond;
            return this;
        }

        public Builder setMonitorReportTime(long millisecond) {
            switchConfig.monitorReportTime = millisecond;
            return this;
        }

        public Builder setSDKLivePeriod(long millisecond) {
            switchConfig.sdkLivePeriod = millisecond;
            return this;
        }

        public Builder setMasterPackageName(String packageName) {
            switchConfig.masterPackageName = packageName;
            return this;
        }

        public Builder setTag(String tag) {
            switchConfig.tag = tag;
            return this;
        }
    }
}
