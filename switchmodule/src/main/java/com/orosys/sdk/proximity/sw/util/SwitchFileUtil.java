package com.orosys.sdk.proximity.sw.util;

import android.content.Context;

import com.orosys.sdk.proximity.sw.SwitchInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oro on 2017. 2. 13..
 */

public class SwitchFileUtil {
    private static final String TAG = SwitchFileUtil.class.getSimpleName();
    private static final String CONFIG_EXE = ".prox";

    /**
     * Get config file
     *
     * @param context
     * @return
     */
    public static File getInfoFile(Context context) {
        File files = context.getExternalFilesDir(null);
        return getInfoFile(files);
    }

    private static File getInfoFile(File files) {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(CONFIG_EXE);
            }
        };

        if (files == null || !files.isDirectory()) {
            return null;
        }
        File[] fileList = files.listFiles(filenameFilter);
        if (fileList == null) {
            return null;
        }

        for (File file : fileList) {
            if (file != null) {
                return file;
            }
        }

        File configFile = new File(files.getAbsolutePath() + "/" + SwitchInfo.INIT_CONFIG + CONFIG_EXE);
        try {
            boolean result = configFile.createNewFile();
            if (!result) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return configFile;
    }

    /**
     * Get config
     *
     * @return SwitchInfo
     */
    public static SwitchInfo getInfo(File configFile) {
        if (configFile == null) {
            return null;
        }
        String[] configAbs = configFile.getAbsolutePath().split("/");
        if (configAbs.length == 0) {
            return null;
        }
        String packageName = configAbs[configAbs.length - 3];
        String config = configAbs[configAbs.length - 1];
        config = config.split("\\.")[0];

        return new SwitchInfo.Builder().setPackageName(packageName).setConfigs(config.split("_")).build();
    }

    /**
     * Save config
     *
     * @param context
     * @param switchInfo
     */
    public static boolean saveInfo(Context context, SwitchInfo switchInfo) {
        File configFile = getInfoFile(context);
        SwitchInfo currentSwitchInfo = getInfo(configFile);
        return configFile.renameTo(new File(configFile.getParent() + "/" + currentSwitchInfo.merged(switchInfo) + CONFIG_EXE));
    }

    public static List<SwitchInfo> getSDKInfoFromExternalStorage(Context context, List<String> sdkList) {
        String extDir = context.getExternalFilesDir(null).getParentFile().getParentFile().getAbsolutePath();
        List<SwitchInfo> switchInfoList = new ArrayList<>();
        for (String packageName : sdkList) {
            File files = new File(extDir + "/" + packageName + "/files");
            SwitchInfo switchInfo = getInfo(getInfoFile(files));
            if (switchInfo == null) {
                switchInfo = new SwitchInfo.Builder().setPackageName(packageName).build();
            }
            switchInfoList.add(switchInfo);
        }

        return switchInfoList;
    }
}
