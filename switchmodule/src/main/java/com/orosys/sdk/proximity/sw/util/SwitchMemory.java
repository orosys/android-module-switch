package com.orosys.sdk.proximity.sw.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oro on 2017. 2. 15..
 */
public class SwitchMemory {
    private static SwitchMemory ourInstance = new SwitchMemory();
    private Map<String, Object> memoryMap;

    public static SwitchMemory getInstance() {
        return ourInstance;
    }

    private SwitchMemory() {
        memoryMap = new HashMap<>();
    }

    public void put(String key, Object value) {
        memoryMap.put(key, value);
    }

    public Object get(String key) {
        return memoryMap.get(key);
    }

    public void remove(String key) {
        memoryMap.remove(key);
    }

    public void clear() {
        memoryMap.clear();
    }
}
