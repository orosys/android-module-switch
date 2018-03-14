package com.orosys.sdk.proximity.sw;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by oro on 2017. 2. 14..
 */

public class SwitchInfo implements Parcelable {
   /**
     * INIT_CONFIG
     *  module on/ off,
     *  group id,
     *  common layer version,
     *  position permission,
     *  user info agree,
     *  status(1:master 2:slave 3:none),
     *  last launch time,
     *  last run time
     */
    public static final String INIT_CONFIG = "0_0_0_0_0_0_0_0";

    private int switchEnable = -1;
    private int groupId = -1;
    private int commonLayerVersion = -1;
    private int positionPermission = -1;
    private int userInfoAgree = -1;
    private int status = -1;
    private long lastLaunchTime;
    private long lastSDKRunTime;
    private String packageName;

    private SwitchInfo() {

    }

    public static SwitchInfo Init() {
        return new SwitchInfo.Builder()
                .setCommonLayerVersion(0)
                .setLastLaunchTime(0)
                .setLastSDKRunTime(0)
                .setGroupId(0)
                .setUserInfoAgree(0)
                .setPositionPermission(0)
                .setEnable(0)
                .setStatus(0)
                .build();
    }

    protected SwitchInfo(Parcel in) {
        switchEnable = in.readInt();
        groupId = in.readInt();
        commonLayerVersion = in.readInt();
        positionPermission = in.readInt();
        userInfoAgree = in.readInt();
        status = in.readInt();
        lastLaunchTime = in.readLong();
        lastSDKRunTime = in.readLong();
        packageName = in.readString();
    }

    public static final Creator<SwitchInfo> CREATOR = new Creator<SwitchInfo>() {
        @Override
        public SwitchInfo createFromParcel(Parcel in) {
            return new SwitchInfo(in);
        }

        @Override
        public SwitchInfo[] newArray(int size) {
            return new SwitchInfo[size];
        }
    };

    public String getPackageName() {
        return packageName;
    }

    public int getCommonLayerVersion() {
        return commonLayerVersion;
    }

    public long getLastLaunchTime() {
        return lastLaunchTime;
    }

    public long getLastSDKRunTime() {
        return lastSDKRunTime;
    }

    public int getUserInfoAgree() {
        return userInfoAgree;
    }

    public int getPositionPermission() {
        return positionPermission;
    }

    public int getStatus() {
        return status;
    }

    public int getSwitchEnable() {
        return switchEnable;
    }

    public int getGroupId() {
        return groupId;
    }

    /**
     * Common layer version check
     *
     * @param switchInfo
     * @return a.isHighVersion(b) = a < b
     */
    public boolean isHighVersion(SwitchInfo switchInfo) {
        return getCommonLayerVersion() < switchInfo.getCommonLayerVersion();
    }

    /**
     * Last launch time check
     *
     * @param switchInfo
     * @return a.isLastLaunch(b) = a < b
     */
    public boolean isLastLaunch(SwitchInfo switchInfo) {
        return getLastLaunchTime() < switchInfo.getLastLaunchTime();
    }

    /**
     * Last run time check
     *
     * @param switchInfo
     * @return a.isLastRun(b) = a < b
     */
    public boolean isLastRun(SwitchInfo switchInfo) {
        return getLastSDKRunTime() < switchInfo.getLastSDKRunTime();
    }

    public boolean isMasterEnable() {
        return getSwitchEnable() > 0 && // module switchEnable
                getPositionPermission() > 0 && // 위치 권한
                getUserInfoAgree() > 0 // 사용자 정보이용 동의
                ;
    }

    public boolean isSlaveEnable() {
        return getSwitchEnable() > 0 && // module switchEnable
                getUserInfoAgree() > 0 // 사용자 정보이용 동의
                ;
    }

    public String merged(SwitchInfo switchInfo) {
        String info = "";
        if (switchInfo.getSwitchEnable() > -1) {
            info += "" + switchInfo.getSwitchEnable();
            switchEnable = switchInfo.getSwitchEnable();
        } else {
            info += "" + getSwitchEnable();
        }

        if (switchInfo.getGroupId() > -1) {
            info += "_" + switchInfo.getGroupId();
            groupId = switchInfo.getGroupId();
        } else {
            info += "_" + getGroupId();
        }

        if (switchInfo.getCommonLayerVersion() > -1) {
            info += "_" + switchInfo.getCommonLayerVersion();
            commonLayerVersion = switchInfo.getCommonLayerVersion();
        } else {
            info += "_" + getCommonLayerVersion();
        }

        if (switchInfo.getPositionPermission() > -1) {
            info += "_" + switchInfo.getPositionPermission();
            positionPermission = switchInfo.getPositionPermission();
        } else {
            info += "_" + getPositionPermission();
        }

        if (switchInfo.getUserInfoAgree() > -1) {
            info += "_" + switchInfo.getUserInfoAgree();
            userInfoAgree = switchInfo.getUserInfoAgree();
        } else {
            info += "_" + getUserInfoAgree();
        }

        if (switchInfo.getStatus() > -1) {
            info += "_" + switchInfo.getStatus();
            status = switchInfo.getStatus();
        } else {
            info += "_" + getStatus();
        }

        if (switchInfo.getLastLaunchTime() > 0) {
            info += "_" + switchInfo.getLastLaunchTime();
            lastLaunchTime = switchInfo.getLastLaunchTime();
        } else {
            info += "_" + getLastLaunchTime();
        }

        if (switchInfo.getLastSDKRunTime() > 0) {
            info += "_" + switchInfo.getLastSDKRunTime();
            lastSDKRunTime = switchInfo.getLastSDKRunTime();
        } else {
            info += "_" + getLastSDKRunTime();
        }

        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(switchEnable);
        parcel.writeInt(groupId);
        parcel.writeInt(commonLayerVersion);
        parcel.writeInt(positionPermission);
        parcel.writeInt(userInfoAgree);
        parcel.writeInt(status);
        parcel.writeLong(lastLaunchTime);
        parcel.writeLong(lastSDKRunTime);
        parcel.writeString(packageName);
    }

    @Override
    public String toString() {
        return "SwitchInfo{" +
                "switchEnable=" + switchEnable +
                ", groupId='" + groupId + '\'' +
                ", commonLayerVersion='" + commonLayerVersion + '\'' +
                ", positionPermission=" + positionPermission +
                ", userInfoAgree=" + userInfoAgree +
                ", status=" + status +
                ", lastLaunchTime=" + lastLaunchTime +
                ", lastSDKRunTime=" + lastSDKRunTime +
                ", packageName='" + packageName + '\'' +
                '}';
    }

    public static class Builder {
        private SwitchInfo switchInfo;

        public Builder() {
            switchInfo = new SwitchInfo();
        }

        public SwitchInfo build() {
            return switchInfo;
        }

        public Builder setConfigs(String[] configs) {
            if (configs == null || configs.length < 8) {
                return this;
            }

            try {
                switchInfo.switchEnable = Integer.parseInt(configs[0]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.groupId = Integer.parseInt(configs[1]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.commonLayerVersion = Integer.parseInt(configs[2]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.positionPermission = Integer.parseInt(configs[3]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.userInfoAgree = Integer.parseInt(configs[4]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.status = Integer.parseInt(configs[5]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.lastLaunchTime = Long.parseLong(configs[6]);
            } catch (NullPointerException | NumberFormatException e) {
            }
            try {
                switchInfo.lastSDKRunTime = Long.parseLong(configs[7]);
            } catch (NullPointerException | NumberFormatException e) {
            }

            return this;
        }

        public Builder setEnable(int enable) {
            switchInfo.switchEnable = enable;
            return this;
        }

        public Builder setGroupId(int id) {
            switchInfo.groupId = id;
            return this;
        }

        public Builder setCommonLayerVersion(int commonLayerVersion) {
            switchInfo.commonLayerVersion = commonLayerVersion;
            return this;
        }

        public Builder setPositionPermission(int positionPermission) {
            switchInfo.positionPermission = positionPermission;
            return this;
        }

        public Builder setUserInfoAgree(int userInfoAgree) {
            switchInfo.userInfoAgree = userInfoAgree;
            return this;
        }

        public Builder setStatus(int status) {
            switchInfo.status = status;
            return this;
        }

        public Builder setLastLaunchTime(long lastLaunchTime) {
            switchInfo.lastLaunchTime = lastLaunchTime;
            return this;
        }

        public Builder setLastSDKRunTime(long lastRunTime) {
            switchInfo.lastSDKRunTime = lastRunTime;
            return this;
        }

        public Builder setPackageName(String packageName) {
            switchInfo.packageName = packageName;
            return this;
        }
    }
}
