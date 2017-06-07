package com.blue.car.manager;

import com.blue.car.service.BlueUtils;
import com.blue.car.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommandRespManager {
    private Map<String, OnDataCallback> commandMap = new HashMap<>();
    private String command;
    private List<byte[]> commandDataList = new ArrayList<>();

    public interface OnDataCallback {
        void resp(byte[] data);
    }

    public void setCommandRespCallBack(String command, OnDataCallback callback) {
        this.command = command;
        if (!commandMap.containsKey(command)) {
            commandMap.put(command, callback);
        }
    }

    public void processCommandResp(byte[] data) {
        processCommandResp(command, data);
    }

    public void addCommandRespCallBack(String command, OnDataCallback callback) {
        commandMap.put(command, callback);
    }

    public void processCommandResp(String command, byte[] data) {
        if (StringUtils.isNullOrEmpty(command) || !commandMap.containsKey(command)) {
            return;
        }
        OnDataCallback callback = commandMap.get(command);
        if (callback != null) {
            callback.resp(data);
        }
    }

    public synchronized byte[] obtainData(byte[] data) {
        boolean result = CommandManager.startWithStartFrameHead(data);
        if (result) {
            result = CommandManager.checkDataComplete(data);
            if (result) {
                return data;
            }
            commandDataList.add(data);
            return null;
        } else {
            commandDataList.add(data);
            List<byte[]> list = commandDataList;
            commandDataList = new ArrayList<>();
            return BlueUtils.byteListToArrayByte(list);
        }
    }
}
