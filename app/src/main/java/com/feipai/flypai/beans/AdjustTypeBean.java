package com.feipai.flypai.beans;

public class AdjustTypeBean {
    private int minBrightness = 95;
    private int maxBrightness = 145;

    private int minContrast = 72;
    private int maxContrast = 170;

    private int minSaturation = 0;
    private int maxSaturation = 100;


    public AdjustTypeBean(int minBrightness, int maxBrightness,
                          int minContrast, int maxContrast,
                          int minSaturation, int maxSaturation) {
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;

        this.minContrast = minContrast;
        this.maxContrast = maxContrast;

        this.minSaturation = minSaturation;
        this.maxSaturation = maxSaturation;
    }


    public int getMinBrightness() {
        return minBrightness;
    }

    public void setMinBrightness(int minBrightness) {
        this.minBrightness = minBrightness;
    }


    public int getMaxBrightness() {
        return maxBrightness;
    }

    public void setMaxBrightness(int maxBrightness) {
        this.maxBrightness = maxBrightness;
    }

    public int getMidBrightness() {
        return (maxBrightness - minBrightness) / 2;
    }

    public int getRealBrightnessProgress(int value) {
        return value - (maxBrightness + minBrightness) / 2;
    }


    public int getRealBrightness(int progress) {
        return progress + (maxBrightness + minBrightness) / 2;
    }

    public int getMinContrast() {
        return minContrast;
    }

    public void setMinContrast(int minContrast) {
        this.minContrast = minContrast;
    }

    public int getMaxContrast() {
        return maxContrast;
    }

    public void setMaxContrast(int maxContrast) {
        this.maxContrast = maxContrast;
    }


    public int getMidContrast() {
        return (maxContrast - minContrast) / 2;
    }

    public int getRealContrastProgress(int value) {
        return value - (maxContrast + minContrast) / 2;
    }


    public int getRealContrast(int progress) {
        return progress + (maxContrast + minContrast) / 2;
    }


    public int getMinSaturation() {
        return minSaturation;
    }

    public void setMinSaturation(int minSaturation) {
        this.minSaturation = minSaturation;
    }

    public int getMaxSaturation() {
        return maxSaturation;
    }

    public void setMaxSaturation(int maxSaturation) {
        this.maxSaturation = maxSaturation;
    }


    public int getMidSaturation() {
        return (maxSaturation - minSaturation) / 2;
    }

    public int getRealSaturationProgress(int value) {
        return value - (maxSaturation + minSaturation) / 2;
    }


    public int getRealSaturation(int progress) {
        return progress + (maxSaturation + minSaturation) / 2;
    }

}
