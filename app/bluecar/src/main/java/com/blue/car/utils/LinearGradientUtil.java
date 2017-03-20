package com.blue.car.utils;

import android.graphics.Color;

public class LinearGradientUtil {

    public static int getColor(int startColor, int endColor, float radio) {
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);

        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }

    public static int getColor(float rotation) {
        int startColor = 0, endColor = 0;
        float tmp = rotation % 360;
        if (tmp <= 60) {
            startColor = Color.RED;
            endColor = Color.YELLOW;
        } else if (tmp <= 120) {
            startColor = Color.YELLOW;
            endColor = Color.GREEN;
        } else if (tmp <= 180) {
            startColor = Color.GREEN;
            endColor = Color.CYAN;
        } else if (tmp < 240) {
            startColor = Color.CYAN;
            endColor = Color.BLUE;
        } else if (tmp <= 300) {
            startColor = Color.BLUE;
            endColor = Color.MAGENTA;
        } else if (tmp <= 360) {
            startColor = Color.MAGENTA;
            endColor = Color.RED;
        }
        float radio = tmp % 60 / 60;
        return LinearGradientUtil.getColor(startColor, endColor, radio);
    }

    public static int[] getActualColor(float rotation) {
        //纯红0，纯绿80，纯蓝160
        int startColor = 0, endColor = 0;
        float tmp = 360 - rotation % 360;
        if (tmp <= 120) {
            startColor = Color.RED;
            endColor = Color.BLUE;
        } else if (tmp <= 240) {
            startColor = Color.BLUE;
            endColor = Color.GREEN;
        } else if (tmp <= 360) {
            startColor = Color.GREEN;
            endColor = Color.RED;
        }
        int actualColor = LinearGradientUtil.getColor(startColor, endColor, tmp % 60 / 60);
        return new int[]{actualColor, (int) (tmp / 360 * 240)};
    }

    public static int[] hsbToColor(int hsb){
        //纯红0，纯绿80，纯蓝160
        int startColor = 0, endColor = 0;
        float tmp = hsb % 240;
        if (tmp <= 80) {
            startColor = Color.RED;
            endColor = Color.GREEN;
        } else if (tmp <= 160) {
            startColor = Color.GREEN;
            endColor = Color.BLUE;
        } else if (tmp <= 240) {
            startColor = Color.BLUE;
            endColor = Color.RED;
        }
        int actualColor = LinearGradientUtil.getColor(startColor, endColor, tmp % 80 / 80);
        return new int[]{actualColor, (int) (tmp / 240 * 360)};
    }
}
