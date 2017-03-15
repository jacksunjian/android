package com.blue.car.manager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.blue.car.model.BatteryInfoCommandResp;
import com.blue.car.model.FirstStartCommandResp;
import com.blue.car.model.LedCommandResp;
import com.blue.car.model.LockConditionInfoCommandResp;
import com.blue.car.model.MainFuncCommandResp;
import com.blue.car.model.RidingTimeCommandResp;
import com.blue.car.model.SensitivityCommandResp;
import com.blue.car.model.SpeedLimitResp;
import com.blue.car.service.BlueUtils;
import com.blue.car.service.BluetoothConstant;

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
    static final byte COMMAND_0C = 0x0C;
    static final byte COMMAND_RECEIVER = 0x0D;

    public static int checkSum(byte[] bytes, int startPosition) {
        return checkSum(bytes, startPosition, bytes.length);
    }

    //not include endPosition, for example [0,n): 0<=p<n
    private static int checkSum(byte[] bytes, int startPosition, int endPosition) {
        int sum = 0;
        for (int i = startPosition; i < endPosition; i++) {
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

    public static byte[] getLimitSpeedSettingCommand(int speedLimitValue) {
        // 《55 AA 04 0A 03 74 88 13 DF FE
        return getSendCommand(BlueUtils.getCharByte(speedLimitValue * 1000), COMMAND_SEND, new byte[]{0x03, 0x74});
    }

    public static byte[] getSensitivityCommand() {
        //《55 AA 03 0A 01 A1 0A 46 FF
        return getSendCommand(new byte[]{0x0A}, COMMAND_SEND, new byte[]{0x01, (byte) 0xA1});
    }

//    public static byte[] getTurnSensitivityCommand(byte[] commandData) {
//        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xA1});
//    }

    public static byte[] getCloseTurnSensitivityCommand() {
        //《55 AA 04 0A 03 A1 32 00 1B FF
        return getSendCommand(new byte[]{0x32, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xA1});
    }

    public static byte[] setTurnSensitivityCommand(int speedTurnValue) {
        // 《55 AA 04 0A 03 A1 32 00 1B FF
        return getSendCommand(BlueUtils.getCharByte(speedTurnValue), COMMAND_SEND, new byte[]{0x03, (byte) 0xA1});
    }

    public static byte[] setRidingSensitivityCommand(int ridingTurnValue) {
        // 《55 AA 04 0A 03 A2 32 00 1A FF
        return getSendCommand(BlueUtils.getCharByte(ridingTurnValue), COMMAND_SEND, new byte[]{0x03, (byte) 0xA2});
    }



    public static byte[] getOpenTurnSensitivityCommand() {
        //《55 AA 04 0A 03 A1 65 00 E8 FE
        return getSendCommand(new byte[]{0x65, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xA1});
    }

//    public static byte[] getRidingSensitivityCommand(byte[] commandData) {
//        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xA2});
//    }

    public static byte[] getCloseRidingSensitivityCommand() {
        //《55 AA 04 0A 03 A2 32 00 1A FF
        return getSendCommand(new byte[]{0x32, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xA2});
    }

    public static byte[] getOpenRidingSensitivityCommand() {
        //《55 AA 04 0A 03 A2 65 00 E7 FE
        return getSendCommand(new byte[]{0x65, 0x00}, COMMAND_SEND, new byte[]{0x03, (byte) 0xA2});
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

    public static byte[] getLockConditionSettingCommand(int status) {
        //for example《55 AA 04 0A 03 D3 13 00 08 FF
        return getSendCommand(BlueUtils.getCharByte(status), COMMAND_SEND, new byte[]{0x03, (byte) 0xD3});
    }

    public static byte[] getLedCommand() {
        //《55 AA 03 0A 01 C6 1A 11 FF
        return getSendCommand(new byte[]{0x1A}, COMMAND_SEND, new byte[]{0x01, (byte) 0xC6});
    }

    // color: 0~239 其中纯红0，纯绿80，纯蓝160
    public static byte[] getLedColorSettingCommand(int color) {
        //《55 AA 04 0A 03 C8 F0 D9 5D FD
        byte[] commandData = new byte[]{(byte) 0xF0, BlueUtils.intToByte(color)};
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xC8});
    }

    /*设置氛围灯模式0-9分别代表关闭、单色呼吸、全彩呼吸、双龙戏珠、全彩分向、单色流星、炫彩流星、警灯模式1-3*/
    public static byte[] getAmbientLightSettingCommand(int mode) {
        //《55 AA 04 0A 03 C6 09 00 28 FF
        byte[] commandData = new byte[]{(byte) mode};
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xC6});
    }

    public static byte[] getFrontLightOpenCommand(int mode) {
        //《55 AA 04 0A 03 D3 03 00 18 FF
        return getFrontBehindLightCommand(new byte[]{0x03, 0x00});
    }

    public static byte[] getFrontLightCloseCommand(int mode) {
        //《55 AA 04 0A 03 D3 02 00 19 FF
        return getFrontBehindLightCommand(new byte[]{0x02, 0x00});
    }

    public static byte[] getBrakesLightOpenCommand(int mode) {
        //《55 AA 04 0A 03 D3 04 00 18 FF
        return getFrontBehindLightCommand(new byte[]{0x04, 0x00});
    }

    public static byte[] getBrakesLightCloseCommand(int mode) {
        //《55 AA 04 0A 03 D3 01 00 1A FF
        return getFrontBehindLightCommand(new byte[]{0x01, 0x00});
    }

    private static byte[] getFrontBehindLightCommand(byte[] commandData) {
        return getSendCommand(commandData, COMMAND_SEND, new byte[]{0x03, (byte) 0xD3});
    }

    public static byte[] getBlueCarNameSettingCommand(@NonNull String name) {
        //55 AA 11 0A 50 00
        //4E 69 6E 65 62 6F 74 4D 69 6E 69 20 4D 31 30  蓝牙名称小于30个字符，asc码表示
        //6A FA
        return getSendCommand(BlueUtils.getBytesByIsoCharsetName(name), COMMAND_SEND, new byte[]{0x50, 0x00});
    }

    public static byte[] getBlueCarPasswordSettingCommand(String password) {
        //《55 AA 08 0A 03 17
        // 31 32 33 34 35 36  密码123456
        // 9E FE
        return getSendCommand(BlueUtils.getBytesByIsoCharsetName(password), COMMAND_SEND, new byte[]{0x03, 0x17});
    }

    public static byte[] getBlueCarPasswordCheckCommand() {
        //《55 AA 03 0A 01 17 06 D4 FF
        //》55 AA 08 0D 01 17 31 32 33 34 35 36 9D FE
        return getSendCommand(new byte[]{0x06}, COMMAND_SEND, new byte[]{0x01, 0x17});
    }

    public static byte[] getBlueCarRidingTimeCommand() {
        //《55 AA 03 0A 01 32 18 21 FF
        return getSendCommand(new byte[]{0x18}, COMMAND_SEND, new byte[]{0x01, 0x32});
    }

    public static byte[] getBatteryInfoCommand() {
        //《55 AA 03 0C 01 31 16 A8 FF
        return getSendCommand(new byte[]{0x16}, COMMAND_0C, new byte[]{0x01, 0x31});
    }

    /*黑匣子读取步骤
    1.锁车
    2.黑匣子读取
    3.解锁*/
    //待定

    public static byte[] getRemoteControlModeCommand() {
        //《55 AA 03 0A 01 B2 08 3D FF
        return getSendCommand(new byte[]{0x08}, COMMAND_SEND, new byte[]{0x01, (byte) 0xB2});
    }

    public static byte[] getRemoteControlOpenCommand() {
        //《55 AA 04 0A 03 7A 01 00 73 FF
        return getSendCommand(new byte[]{0x01, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x7A});
    }

    public static byte[] getRemoteControlCloseCommand() {
        //《55 AA 04 0A 03 7A 00 00 74 FF
        return getSendCommand(new byte[]{0x00, 0x00}, COMMAND_SEND, new byte[]{0x03, 0x7A});
    }

    public static byte[] getRemoteControlMoveCommand(int xValue, int yValue) {
        //《55 AA 06 0A 03 7B 00 00 00 00 71 FF
        //《55 AA 06 0A 03 7B CF 07 EE FF AE FC
        byte[] xBytes = BlueUtils.getCharByte(xValue);
        byte[] yBytes = BlueUtils.getCharByte(yValue);
        return getSendCommand(BlueUtils.getNewBytes(xBytes, yBytes), COMMAND_SEND, new byte[]{0x03, 0x7B});
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
        int sum = checkSum(result, 2);//帧头0x55 0xaa不需要
        byte[] checkByte = BlueUtils.getCharByte(~sum);
        index += data.length;
        setValue(result, index, checkByte);
        Log.d("checkSum", toHexString(BlueUtils.getStringByIsoCharsetName(checkByte)));
        Log.d("command", toHexString(BlueUtils.getStringByIsoCharsetName(result)));
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

    /*55 AA 20 0D 01 10
      31 31 39 36 30 2F 31 30 30 30 30 39 30 30 carId(14位)
      30 30 30 30 30 30  passWord
      31 01 //主板版本号
      00 00 //错误码
      00 00 //警告码
      48 18 sysStatus
      89 01 be[2]
      CB FA*/
    public static FirstStartCommandResp getFirstStartCommandRespData(@NonNull byte[] originData) {
        /*byte[] originData = UnEncryptAndCheckSumData(encryptData);
        if (originData == null) {
            return null;
        }*/
        FirstStartCommandResp resp = new FirstStartCommandResp();
        resp.carId = BlueUtils.bytesToAscii(originData, 6, 14);
        resp.blePassword = BlueUtils.bytesToAscii(originData, 20, 6);
        resp.boardVersion = BlueUtils.byteArrayToInt(originData, 26, 2);
        resp.errCode = BlueUtils.byteArrayToInt(originData, 28, 2);
        resp.warningCode = BlueUtils.byteArrayToInt(originData, 30, 2);
        resp.sysStatus = BlueUtils.byteArrayToInt(originData, 32, 2);
        resp.remain = BlueUtils.getNewBytes(originData, 34, 2);
        return resp;
    }

    /*55 AA 22 0D 01 B0
    00 00 //错误代码
    00 00 //警告代码
    48 18 //系统状态
    01 00 //工作模式
    61 00 //剩余电量百分比
    00 00 //速度
    00 00 //平均速度
    61 15 //总里程
    00 00 //本次里程
    00 00 //本次运行时间
    A8 02 //温度
    AA 00 //当前模式的限速值
    64 B4 //电流
    00 00 //b17[2];
    00 00 //MaxAbsKMH
    7B FB*/

    public static MainFuncCommandResp getMainFuncCommandResp(@NonNull byte[] originData) {
        MainFuncCommandResp resp = new MainFuncCommandResp();
        resp.errCode = BlueUtils.byteArrayToInt(originData, 6, 2);
        resp.warningCode = BlueUtils.byteArrayToInt(originData, 8, 2);
        resp.sysStatus = BlueUtils.byteArrayToInt(originData, 10, 2);
        resp.workMode = BlueUtils.byteArrayToInt(originData, 12, 2);
        resp.remainBatteryPercent = BlueUtils.byteArrayToInt(originData, 14, 2);
        resp.speed = BlueUtils.byteArrayToInt(originData, 16, 2) * 1.0f / 1000;
        resp.averageSpeed = BlueUtils.byteArrayToInt(originData, 18, 2) * 1.0f / 1000;
        resp.totalMileage = BlueUtils.byteArrayToInt(originData, 20, 4) * 1.0f / 1000;
        resp.perMileage = BlueUtils.byteArrayToInt(originData, 24, 2) * 1.0f / 100;
        resp.perRunTime = BlueUtils.byteArrayToInt(originData, 26, 2);
        resp.temperature = BlueUtils.byteArrayToInt(originData, 28, 2) * 1.0f / 10;
        resp.speedLimit = BlueUtils.byteArrayToInt(originData, 30, 2) * 1.0f / 1000;
        resp.electricCurrent = BlueUtils.byteArrayToInt(originData, 32, 2);
        resp.remain = BlueUtils.getNewBytes(originData, 34, 2);
        resp.maxAbsSpeed = BlueUtils.byteArrayToInt(originData, 36, 2) * 1.0f / 1000;
        return resp;
    }

    /*
    55 AA 06 0D 01 73
    20 4E //speed?
    88 13 //限速模式下的限速值
    6F FE*/
    public static SpeedLimitResp getSpeedLimitCommandResp(@NonNull byte[] originData) {
        SpeedLimitResp resp = new SpeedLimitResp();
        resp.speed = BlueUtils.byteArrayToInt(originData, 6, 2) / 1000;
        resp.speedLimit = BlueUtils.byteArrayToInt(originData, 8, 2) / 1000;
        return resp;
    }

    /*
    55 AA 0C 0D 01 A1
    65 00 //转弯手把灵敏度
    2D 00 //骑行灵敏度
    11 00 //助力模式下的平衡点
    00 00 //a83[2];
    01 00 //a84 int16u
    A0 FE*/
    public static SensitivityCommandResp getSensitivityCommandResp(@NonNull byte[] originData) {
        SensitivityCommandResp resp = new SensitivityCommandResp();
        resp.turningSensitivity = BlueUtils.byteArrayToInt(originData, 6, 2);
        resp.ridingSensitivity = BlueUtils.byteArrayToInt(originData, 8, 2);
        resp.balanceInPowerMode = BlueUtils.byteArrayToInt(originData, 10, 2);
        resp.remainA = BlueUtils.getNewBytes(originData, 12, 2);
        resp.remainB = BlueUtils.byteArrayToInt(originData, 14, 2);
        return resp;
    }

    /*55 AA 04 0D 01 D3
    07 00
    13 FF
    */
    public static LockConditionInfoCommandResp getLockConditionCommandResp(byte[] originData) {
        LockConditionInfoCommandResp resp = new LockConditionInfoCommandResp();
        resp.alarmStatus = BlueUtils.byteArrayToInt(originData, 6, 2);
        return resp;

    }

    /*55 AA 1C 0D 01 C6
    01 00
    00 00 F0 8B
    00 00 F0 50
    00 00 F0 F0
    00 00 F0 C8

    00 00 00 00
    00 00 00 00
    BB F8*/
    public static LedCommandResp getLedCommandResp(byte[] originData) {
        LedCommandResp resp = new LedCommandResp();
        resp.ledMode = BlueUtils.byteArrayToInt(originData, 6, 2);
        for (int i = 0; i < resp.ledColor.length; i++) {
            resp.ledColor[i] = BlueUtils.byteArrayToInt(originData, 8 + 4 * i, 4);
        }
        return resp;
    }

    /*》55 AA 08 0D 01 17
    31 32 33 34 35 36
    9D FE*/
    public static String getBlueCarPasswordCheckCommandResp(@NonNull byte[] originData) {
        byte[] password = BlueUtils.getNewBytes(originData, 6, 6);
        return BlueUtils.getStringByIsoCharsetName(password);
    }

    public static RidingTimeCommandResp getRidingTimeCommandResp(@NonNull byte[] originData) {
        RidingTimeCommandResp resp = new RidingTimeCommandResp();
        resp.totalRunningTime = BlueUtils.byteArrayToInt(originData, 6, 4);
        resp.totalRidingTime = BlueUtils.byteArrayToInt(originData, 10, 4);
        resp.totalPowerTime = BlueUtils.byteArrayToInt(originData, 14, 4);
        resp.totalRemoteTime = BlueUtils.byteArrayToInt(originData, 18, 4);
        resp.perRunningTime = BlueUtils.byteArrayToInt(originData, 22, 2);
        resp.perRidingTime = BlueUtils.byteArrayToInt(originData, 24, 2);
        resp.perPowerTime = BlueUtils.byteArrayToInt(originData, 26, 2);
        resp.perRemoteTime = BlueUtils.byteArrayToInt(originData, 28, 2);
        return resp;
    }

    /*55 AA 18 0F 01 31
    29 0A 3C 00 08 00 76 16 21 21 00 00 00 00 00 00 29 0A 29 0A 62 00
    99 FD*/
    public static BatteryInfoCommandResp getBatteryInfoCommandResp(@NonNull byte[] originData) {
        BatteryInfoCommandResp resp = new BatteryInfoCommandResp();
        resp.remainBatteryElectricity = BlueUtils.byteArrayToInt(originData, 6, 2);
        resp.remainPercent = BlueUtils.byteArrayToInt(originData, 8, 2);
        resp.electricCurrent = (BlueUtils.byteArrayToInt(originData, 10, 2) * 1.0f) / 1000;
        resp.voltage = (BlueUtils.byteArrayToInt(originData, 12, 2) * 1.0f) / 100;
        int tmp1 = BlueUtils.byteArrayToInt(originData, 14, 1);
        int tmp2 = BlueUtils.byteArrayToInt(originData, 15, 1);
        resp.temperature = (tmp1 + tmp2) / 2 - 20;

        //   resp.temperature = BlueUtils.byteArrayToInt(originData, 14, 2);
        resp.remainForFuture = BlueUtils.getNewBytes(originData, 16, 10);
        resp.state = BlueUtils.byteArrayToInt(originData, 26, 2);
        return resp;
    }

    public static byte[] unEncryptData(byte[] encryptData) {
        byte[] originData = getUnEncryptData(encryptData);
        if (originData == null) {
            return null;
        }
        return originData;
    }

    public static boolean checkVerificationCode(byte[] data) {
        if (data == null || data.length <= 0) {
            return false;
        }
        if (BluetoothConstant.USE_DEBUG) {
            Log.e("dataBeforeCheckSum", BlueUtils.bytesToHexString(data));
        }
        int endPosition = data.length - 2;
        byte[] checkBytes = BlueUtils.getCharByte(~checkSum(data, 2, endPosition));
        return checkBytes[0] == data[endPosition] && checkBytes[1] == data[endPosition + 1];
    }

    public static byte[] getUnEncryptData(byte[] encryptData) {
        if (encryptData == null || encryptData.length <= 0) {
            return null;
        }
        byte[] data = new byte[encryptData.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) EncryptDataManager.getUnEncryptData(0xFF & encryptData[i]);
        }
        return data;
    }

    public static boolean startWith(byte[] src, byte desc[]) {
        return BlueUtils.equalBytes(src, desc, 0, desc.length);
    }

    public static boolean startWithStartFrameHead(byte[] src) {
        return startWith(src, COMMAND_START);
    }

    public static boolean checkDataComplete(byte[] src) {
        int dataLength = (src[2] & 0xFF) - 2;
        return (COMMAND_START.length + 1 + 3 + dataLength + 2) == src.length;
    }
}
