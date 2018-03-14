package com.orosys.sdk.proximity.sw.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by oro on 2017. 2. 10..
 */

public class SwitchPreference {
    private final Context context;
    private static Object mThis = null;

    private SwitchPreference(Context context) {
        this.context = context;
    }

    public static SwitchPreference getInstance(Context context) {
        if (mThis == null) {
            mThis = new SwitchPreference(context);
        }
        return (SwitchPreference) mThis;
    }

    public void save(String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(SwitchPreference.class.getSimpleName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void save(String key, Parcelable parcel) {
        Parcel p = Parcel.obtain();
        p.writeParcelable(parcel, 0);
        String data = base64Encode(p.marshall());
        save(key, data);
    }

    public String get(String key) {
        SharedPreferences prefs = context.getSharedPreferences(SwitchPreference.class.getSimpleName(), MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public void save(String key, List<? extends Parcelable> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        String data = "";
        for (Parcelable parcel : list) {
            Parcel p = Parcel.obtain();
            p.writeParcelable(parcel, 0);
            if ("".equals(data)) {
                data = base64Encode(p.marshall());
            } else {
                data += "," + base64Encode(p.marshall());
            }
        }

        save(key, data);
    }

    public List<Object> getparcelableList(Class<?> cls, String key) {
        List<Object> list = new ArrayList<>();
        String data = get(key);
        if (data == null || data.length() == 0) {
            return list;
        }
        String[] dataList = data.split(",");
        for (String s : dataList) {
            list.add(getParcelableData(cls, s));
        }
        return list;
    }

    public Object getParcelable(Class<?> cls, String key) {
        return getParcelableData(cls, get(key));
    }

    private Object getParcelableData(Class<?> cls, String data) {
        if (data == null || data.length() == 0) {
            return null;
        }
        Parcel p = Parcel.obtain();
        try {
            byte[] bytes = base64Decode(data);
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            return p.readParcelable(cls.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        return null;
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, 2);
    }

    public static byte[] base64Decode(String s) throws IOException {
        return Base64.decode(s, 2);
    }
}
