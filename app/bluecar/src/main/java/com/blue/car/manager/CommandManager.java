package com.blue.car.manager;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class CommandManager {

    /*车体设置命令：
            《《55 AA 04 0A 03 +命令+(2个字节data) +2字节校验（设置某命令字的2个字节数据）
            最后2字节为校验值，是指前面除了祯头55 aa以外所有数据的累加取反，小端模式，先低字节后高字节。
    蓝牙名称设置命令：
            《《55 AA (N+2) 0A 50 00 +(N个字N 蓝牙名称) +2字节校验（设置N个字节数据）
    蓝牙连接后进入主界面前第一条命令：
            《《55 AA 03 0A 01 10 1E C3 FF
    */

    static final byte[] COMMAND_START = {0x55, (byte) 0xAA};
    static final byte COMMAND_SEND = 0x0A;
    static final byte COMMAND_RECEIVER = 0x0D;

    private static int checkSum(byte[] bytes, int offset) {
        int sum = 0;
        for (int i = offset; i < bytes.length; i++) {
            sum += bytes[i] & 0xFF;
        }
        return sum;
    }

    public static byte[] getNameSettingCommand(String name) {
        return getSendCommand(name.getBytes(), COMMAND_SEND, new byte[]{0x50, 0x00});
    }

    public static byte[] getFirstCommand() {
        //55 AA 03 0A 01 10 1E C3 FF
        return getSendCommand(new byte[]{0x1E}, COMMAND_SEND, new byte[]{0x01, 0x10});
    }

    public static byte[] getMainFuncCommand() {
        //55 AA 03 0A 01 B0 20 21 FF
        return getSendCommand(new byte[]{0x20}, COMMAND_SEND, new byte[]{0x01, (byte) 0xB0});
    }

    public static byte[] getLockCarCommand() {
        //《55 AA 04 0A 03 70 01 00 7D FF
        return getSendCommand(new byte[]{0x01, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x70});
    }

    public static byte[] getUnLockCarCommand() {
        //《55 AA 04 0A 03 71 01 00 7C FF
        return getSendCommand(new byte[]{0x01, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x71});
    }

    public static byte[] getLimitSpeedCommand() {
        //《55 AA 04 0A 03 72 01 00 7B FF
        return getSendCommand(new byte[]{0x01, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x72});
    }

    public static byte[] getUnLimitSpeedCommand() {
        //《55 AA 04 0A 03 72 00 00 7C FF
        return getSendCommand(new byte[]{0x00, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x72});
    }

    public static byte[] getQueryLimitSpeedCommand() {
        //《55 AA 03 0A 01 73 04 7A FF
        return getSendCommand(new byte[]{0x04}, COMMAND_SEND, new byte[]{0x01, 0x73});
    }

    public static byte[] getLimitSpeedSettingCommand() {
        // 《55 AA 04 0A 03 74 88 13 DF FE
        return getSendCommand(new byte[]{(byte) 0x88, 0x13}, COMMAND_SEND, new byte[]{0x03, 0x74});
    }

    public static byte[] getSensitivityCommand() {
        //《55 AA 03 0A 01 A1 0A 46 FF
        return getSendCommand(new byte[]{0x0A}, COMMAND_SEND, new byte[]{0x01, (byte) 0xA1});
    }

    public static byte[] getTurnSensitivityCommand(byte[] commandData) {
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xA1});
    }

    public static byte[] getOpenTurnSensitivityCommand() {
        //《55 AA 04 0A 03 A1 32 00 1B FF
        return getTurnSensitivityCommand(new byte[]{0x32, 0x00});
    }

    public static byte[] getCloseTurnSensitivityCommand() {
        //《55 AA 04 0A 03 A1 65 00 E8 FE
        return getTurnSensitivityCommand(new byte[]{0x65, 0x00});
    }

    public static byte[] getRidingSensitivityCommand(byte[] commandData) {
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xA2});
    }

    public static byte[] getOpenRidingSensitivityCommand() {
        //《55 AA 04 0A 03 A2 32 00 1A FF
        return getRidingSensitivityCommand(new byte[]{0x32, 0x00});
    }

    public static byte[] getCloseRidingSensitivityCommand() {
        //《55 AA 04 0A 03 A2 65 00 E7 FE
        return getRidingSensitivityCommand(new byte[]{0x65, 0x00});
    }

    public static byte[] getPowerBalanceSettingCommand(byte[] commandData) {
        /*《55 AA 04 0A 03 A3 F1 FF 5B FD（-1.5度） 0xFFF1 疑惑
        《55 AA 04 0A 03 A3 32 00 19 FF（5.0度）*/
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xA3});
    }

    public static byte[] getLockConditionCommand() {
        //《55 AA 03 0A 01 D3 02 1C FF
        return getSendCommand(new byte[]{0x02}, COMMAND_SEND, new byte[]{0x01, (byte) 0xD3});
    }

    public static byte[] getLockSystemStartCommand() {
        //《55 AA 04 0A 03 D3 13 00 08 FF
        return getSendCommand(new byte[]{0x13, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xD3});
    }

    public static byte[] getLockSystemNotAlarmCommand() {
        //《55 AA 04 0A 03 D3 03 00 18 FF
        return getSendCommand(new byte[]{0x03, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xD3});
    }

    /*
        1《55 AA 04 0A 03 70 01 00 7D FF
        2《55 AA 04 0A 03 75 01 00 78 FF
        3《55 AA 04 0A 03 71 01 00 7C FF
    */

    /*
        byte a = (byte) 0x88;
        int b = 0x88;
        int c = 0xFF & a;
        byte d = -86;
        Log.e("byte0x88", String.valueOf(((byte) 0xAA)));
        Log.e("byte2Binary", Integer.toBinaryString((a & 0xFF) + 0x100).substring(1));
        Log.e("int2Binary", Integer.toBinaryString(b));
        Log.e("int2Binary", Integer.toBinaryString(c));
        Log.e("int2Binary", Integer.toBinaryString(d));
      02-15 21:41:14.888 4502-4502/com.extral.bluecar E/byte0x88: -86
      02-15 21:33:27.228 27550-27550/com.extral.bluecar E/byte2Binary: 10001000
      02-15 21:33:27.228 27550-27550/com.extral.bluecar E/int2Binary: 10001000
      02-15 21:33:27.228 27550-27550/com.extral.bluecar E/int2Binary: 10001000
      02-15 21:41:14.888 4502-4502/com.extral.bluecar E/int2Binary: 1 10101010
      */

    private static byte[] getSendCommand(byte[] data, byte commandResult, byte[] commandSet) {
        byte[] result = new byte[data.length + 8];
        result[0] = COMMAND_START[0];
        result[1] = COMMAND_START[1];
        result[2] = (byte) (data.length + 2);
        result[3] = commandResult;
        int index = 4;
        setValue(result, index, commandSet);
        index += commandSet.length;
        setValue(result, index, data);
        int sum = checkSum(result, 2);
        byte[] checkByte = getCharByte(~sum);
        index += data.length;
        setValue(result, index, checkByte);
        Log.d("checkSum", toHexString(getStringByIsoCharsetName(checkByte)));
        Log.d("command", toHexString(getStringByIsoCharsetName(result)));
        return result;
    }

    private static void setValue(byte[] targetBytes, int resultOffset, byte[] originBytes) {
        for (int i = 0; i < originBytes.length; i++) {
            targetBytes[resultOffset + i] = originBytes[i];
        }
    }

    private static byte[] getIntBytes(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF),
        };
    }

    private static byte[] getCharByte(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }

    public static String toHexString(String originString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < originString.length(); i++) {
            int ch = (int) originString.charAt(i);
            String s = Integer.toHexString(ch);
            if (s.length() == 1) {
                s = "0" + s;
            }
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

    public static String getStringByIsoCharsetName(byte[] bytes) {
        try {
            return new String(bytes, "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
