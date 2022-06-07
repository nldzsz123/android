package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.common.msg_battery_status;
import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.LogUtils;

import java.util.Arrays;

public class BatteryBean extends BaseEntity implements BaseMavlinkEntity {


    private float maxBat = 4.2f;
    private float midBat = 2.8f;
    private float minBat = 2.0f;
    private int[] batteryBg = new int[]{R.drawable.fill00cf00_bg, R.drawable.fillfe9700_bg, R.drawable.fillf34235_bg};
    /**
     * 消息ID
     */
    private int msgId;

    /**
     * 当前电量
     */
    private int curBattey;
    /**
     * 电池电量容量
     */
    private int capacity;

    /**
     * 水平飞行距离
     */
    private String flyDistance;
    /**
     * 最大水平距离
     */
    private int maxFlyDistance;
    /**
     * 飞行高度
     */
    private String flyAlt;
    /**
     * 最大飞行高度
     */
    private int maxAlt;
    /**
     * 返航高度
     */
    private int retureAlt;
    /**
     * 卫星颗数
     */
    private int satellites;
    /**
     * 水平速度
     */
    private String horizontalVelocity;
    /**
     * 飞行状态
     */
    private int flyStatus;
    /**
     * 飞行模式
     */
    private int customMode;
    /**
     * 与全景有关的
     */
    private int yawMoveStatus;
    /**
     * 是否已解锁
     */
    private boolean unlocked;
    /**
     * 遥控是否已连接
     */
    private boolean isRcConnented;

    private int batteryCycle;
    private int temperature;

    private float[] battery;

    /**
     * 电压
     */
    private String voltage;

    private String electricity;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_battery_status batteryMsg = (msg_battery_status) msg;
//        LogUtils.d("读取电池Mavlink消息===>" + batteryMsg.toString());
        setMsgId(batteryMsg.msgid);
        setCapacity(batteryMsg.remaining_mah);
        setCurBattey(batteryMsg.battery_remaining);
        setSatellites(batteryMsg.gps_raw_int_satellites_visible);
        setFlyAlt(ConvertUtils.floatToString(batteryMsg.vfr_hud_alt));
        setFlyDistance(batteryMsg.local_position_ned_x, batteryMsg.local_position_ned_y);
        setHorizontalVelocity(ConvertUtils.floatToString(batteryMsg.vfr_hud_groundspeed));
        setRetureAlt(batteryMsg.rtl_alt_mobile);
        setRcConnented(batteryMsg.rc_status != 0);
        setUnlocked((batteryMsg.heartbeat_base_mode >> 7 & 1) == 1);
        setCustomMode(batteryMsg.heartbeat_custom_mode);
        setFlyStatus(batteryMsg.fly_status);
        setYawMoveStatus(batteryMsg.yaw_move_status);
        setBatteryCycle(batteryMsg.cycle_count);//循环次数
        setTemperature(batteryMsg.temperature);//温度
        setElectricity(String.valueOf(Math.abs(batteryMsg.current_battery * 10)));
        short[] voltages = batteryMsg.voltages;
        setBattery(voltages);

    }


    @Override
    public int getMsgId() {
        return msgId;
    }


    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurBattey() {
        return curBattey;
    }

    public void setCurBattey(int curBattey) {
        this.curBattey = curBattey;
    }

    public String getFlyDistance() {
        return flyDistance;
    }

    public void setFlyDistance(float x, float y) {
        float levelValue = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        this.flyDistance = ConvertUtils.floatToString(levelValue);
    }

    public int getMaxFlyDistance() {
        return maxFlyDistance;
    }

    public void setMaxFlyDistance(int maxFlyDistance) {
        this.maxFlyDistance = maxFlyDistance;
    }

    public String getFlyAlt() {
        return flyAlt;
    }

    public void setFlyAlt(String flyAlt) {
        this.flyAlt = flyAlt;
    }

    public int getMaxAlt() {
        return maxAlt;
    }

    public void setMaxAlt(int maxAlt) {
        this.maxAlt = maxAlt;
    }

    public int getRetureAlt() {
        return retureAlt;
    }

    public void setRetureAlt(int retureAlt) {
        this.retureAlt = retureAlt;
    }

    public int getSatellites() {
        return satellites;
    }

    public void setSatellites(int satellites) {
        this.satellites = satellites;
    }

    public String getHorizontalVelocity() {
        return horizontalVelocity;
    }

    public void setHorizontalVelocity(String horizontalVelocity) {
        this.horizontalVelocity = horizontalVelocity;
    }

    public int getFlyStatus() {
        return flyStatus;
    }

    public void setFlyStatus(int flyStatus) {
        this.flyStatus = flyStatus;
    }

    public int getCustomMode() {
        return customMode;
    }

    public void setCustomMode(int customMode) {
        this.customMode = customMode;
    }

    public int getYawMoveStatus() {
        return yawMoveStatus;
    }

    public void setYawMoveStatus(int yawMoveStatus) {
        this.yawMoveStatus = yawMoveStatus;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isRcConnented() {
        return isRcConnented;
    }

    public void setRcConnented(boolean rcConnented) {
        isRcConnented = rcConnented;
    }

    public void setFlyDistance(String flyDistance) {
        this.flyDistance = flyDistance;
    }

    public int getBatteryCycle() {
        return batteryCycle;
    }

    public void setBatteryCycle(int batteryCycle) {
        this.batteryCycle = batteryCycle;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public float[] getBattery() {
        return battery;
    }

    public boolean isReturnMode() {
        return isRcConnented() && getCustomMode() != 6 && getFlyStatus() == 24;
    }

    public void setBattery(short[] voltages) {
        this.battery = new float[voltages.length];
        battery[0] = voltages[0] / 1000f;
        battery[1] = voltages[1] / 1000f;
        battery[2] = voltages[2] / 1000f;
        battery[3] = voltages[3] / 1000f;
        setVoltage(ConvertUtils.floatToString("%.2f", battery[0] + battery[1] + battery[2] + battery[3]));
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public float getFirstVoltageScale() {
        return battery[0] / maxBat;
    }

    public float getSecondVoltageScale() {
        return battery[1] / maxBat;
    }

    public float getThirdVoltageScale() {
        return battery[2] / maxBat;
    }

    public float getFourthVoltageScale() {
        return battery[3] / maxBat;
    }

    public int getFirstBatteryBg() {
        float bat = battery[0];
        if (bat >= midBat) {
            return batteryBg[0];
        } else if (bat >= minBat && bat < midBat) {
            return batteryBg[1];
        } else {
            return batteryBg[2];
        }
    }

    public int getSecondBatteryBg() {
        float bat = battery[1];
        if (bat >= midBat) {
            return batteryBg[0];
        } else if (bat >= minBat && bat < midBat) {
            return batteryBg[1];
        } else {
            return batteryBg[2];
        }
    }

    public int getFourthBatteryBg() {
        float bat = battery[2];
        if (bat >= midBat) {
            return batteryBg[0];
        } else if (bat >= minBat && bat < midBat) {
            return batteryBg[1];
        } else {
            return batteryBg[2];
        }
    }

    public int getThirdBatteryBg() {
        float bat = battery[3];
        if (bat >= midBat) {
            return batteryBg[0];
        } else if (bat >= minBat && bat < midBat) {
            return batteryBg[1];
        } else {
            return batteryBg[2];
        }
    }


    public String getElectricity() {
        return electricity;
    }

    public void setElectricity(String electricity) {
        this.electricity = electricity;
    }

    @Override
    public String toString() {
        return "电池包{" +
                "msgId=" + msgId +
                ", capacity=" + capacity +
                ", curBattey=" + curBattey +
                ", flyDistance='" + flyDistance + '\'' +
                ", maxFlyDistance=" + maxFlyDistance +
                ", flyAlt='" + flyAlt + '\'' +
                ", maxAlt=" + maxAlt +
                ", retureAlt=" + retureAlt +
                ", satellites=" + satellites +
                ", horizontalVelocity='" + horizontalVelocity + '\'' +
                ", flyStatus=" + flyStatus +
                ", customMode=" + customMode +
                ", yawMoveStatus=" + yawMoveStatus +
                ", unlocked=" + unlocked +
                ", isRcConnented=" + isRcConnented +
                ", batteryCycle=" + batteryCycle +
                ", temperature=" + temperature +
                ", battery=" + Arrays.toString(battery) +
                ", voltage='" + voltage + '\'' +
                ", electricity='" + electricity + '\'' +
                '}';
    }
}
