package com.blue.car.utils;

import java.util.Collection;
import java.util.Map;

public class CollectionUtils {

    public static boolean isNullOrEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
