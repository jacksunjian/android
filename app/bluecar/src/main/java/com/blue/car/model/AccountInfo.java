package com.blue.car.model;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.blue.car.manager.SecurePreferences;
import com.blue.car.utils.StringUtils;

import java.io.Serializable;

public class AccountInfo implements Serializable {

    public static String SECURE_TYPE_ACCOUNT = AccountInfo.class.getSimpleName();
    public static String SECURE_ACCOUNT_TAG = "blue_car_account_info";
    public static String SECURE_ACCOUNT_KEY = "account_json_string";

    private static AccountInfo accountInfo;

    public String bleName;
    public String blePassword;

    static public AccountInfo currentAccountInfo(Context context) {
        if (accountInfo != null) {
            return accountInfo;
        }
        return accountInfo = loadAccountInfo(context);
    }

    static public void saveAccountInfo(Context context, AccountInfo account) {
        accountInfo = account;
        saveToSecurePreferences(context, account);
    }

    static public void saveAccountInfo(Context context, String bleName, String blePassword) {
        AccountInfo accountInfo = currentAccountInfo(context);
        if (accountInfo == null) {
            accountInfo = new AccountInfo();
        }
        accountInfo.bleName = bleName;
        accountInfo.blePassword = blePassword;
        saveAccountInfo(context, accountInfo);
    }

    static private AccountInfo loadAccountInfo(final Context context) {
        SecurePreferences preferences = new SecurePreferences(context, SECURE_TYPE_ACCOUNT, SECURE_ACCOUNT_TAG, true);
        final String string = preferences.getString(SECURE_ACCOUNT_KEY);
        if (StringUtils.isNullOrEmpty(string)) {
            return new AccountInfo();
        }
        return JSON.parseObject(string, AccountInfo.class);
    }

    static private void saveToSecurePreferences(Context context, AccountInfo accountInfo) {
        SecurePreferences preferences = new SecurePreferences(context, SECURE_TYPE_ACCOUNT, SECURE_ACCOUNT_TAG, true);
        preferences.put(SECURE_ACCOUNT_KEY, JSON.toJSONString(accountInfo));
    }
}
