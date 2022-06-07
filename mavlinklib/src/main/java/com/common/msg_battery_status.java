// MESSAGE BATTERY_STATUS PACKING
package com.common;

import com.MAVLinkPacket;
import com.Messages.MAVLinkMessage;
import com.Messages.MAVLinkPayload;

/**
 * Battery information
 * 此包现包括所有状态
 */
public class msg_battery_status extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_BATTERY_STATUS = 147;
    public static final int MAVLINK_MSG_LENGTH = 104;
    private static final long serialVersionUID = MAVLINK_MSG_ID_BATTERY_STATUS;


    /**
     * Consumed charge, in milliampere hours (1 = 1 mAh), -1: autopilot does not provide mAh consumption estimate
     */
    public int current_consumed;
    /**
     * Consumed energy, in 100*Joules (intergrated U*I*dt)  (1 = 100 Joule), -1: autopilot does not provide energy consumption estimate
     */
    public int energy_consumed;

    /**
     * HEARTBEAT A bitfield for use for autopilot-specific flags
     */
    public int heartbeat_custom_mode;

    public float vfr_hud_alt;
    public float vfr_hud_climb;
    public float vfr_hud_groundspeed;
    public float local_position_ned_x;
    public float local_position_ned_y;
    public float local_position_ned_z;
    public float local_position_ned_vx;
    public float local_position_ned_vy;
    public float local_position_ned_vz;
    public float attitude_roll;
    public float attitude_pitch;


    /**
     * Temperature of the battery in centi-degrees celsius. INT16_MAX for unknown temperature.
     */
    public short temperature;

    /**
     * Battery voltage of cells, in millivolts (1 = 1 millivolt)
     */
    public short voltages[] = new short[10];

    /**
     * Battery current, in 10*milliamperes (1 = 10 milliampere), -1: autopilot does not measure the current
     */
    public short current_battery;

    public short remaining_mah;//剩余电量

    public short cycle_count;


    public short full_charge;

    public short fence_radius_mobile; /*< aircraft fence radius*/
    public short fence_rtl_max_mobile; /*< aircraft fence rtl alt*/
    public short rtl_alt_mobile; /*< aircraft fence rtl alt*/
    public short vfr_hud_heading;

    /**
     * Battery ID
     */
    public byte id;
    /**
     * Function of the battery
     */
    public byte function;
    /**
     * Type (chemistry) of the battery
     */
    public byte type;
    /**
     * Remaining battery energy: (0%: 0, 100%: 100), -1: autopilot does not estimate the remaining battery
     */
    public byte battery_remaining;


    public byte fly_status;

    public byte yaw_move_status;
    public byte get_fence_status;//是否撞到围栏 1：撞到，0：未撞到

    /*< HEARTBEAT System mode bitfield, see MAV_MODE_FLAG ENUM in mavlink/include/mavlink_types.h*/
    public byte heartbeat_base_mode;
    /*< GPS_RAW_INT Number of satellites visible. If unknown, set to 255*/
    public byte gps_raw_int_satellites_visible;

    public byte rc_status;


    /**
     * Generates the payload for a mavlink message for a message of this type
     *
     * @return
     */
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_BATTERY_STATUS;
        packet.payload.putInt(current_consumed);
        packet.payload.putInt(energy_consumed);
        packet.payload.putInt(heartbeat_custom_mode);
        packet.payload.putFloat(vfr_hud_alt);
        packet.payload.putFloat(vfr_hud_climb);
        packet.payload.putFloat(vfr_hud_groundspeed);
        packet.payload.putFloat(local_position_ned_x);
        packet.payload.putFloat(local_position_ned_y);
        packet.payload.putFloat(local_position_ned_z);
        packet.payload.putFloat(local_position_ned_vx);
        packet.payload.putFloat(local_position_ned_vy);
        packet.payload.putFloat(local_position_ned_vz);
        packet.payload.putFloat(attitude_roll);
        packet.payload.putFloat(attitude_pitch);
        packet.payload.putShort(temperature);
        for (int i = 0; i < voltages.length; i++) {
            packet.payload.putShort(voltages[i]);
        }
        packet.payload.putShort(current_battery);
        packet.payload.putShort(remaining_mah);
        packet.payload.putShort(cycle_count);
        packet.payload.putShort(full_charge);
        packet.payload.putShort(fence_radius_mobile);
        packet.payload.putShort(fence_rtl_max_mobile);
        packet.payload.putShort(rtl_alt_mobile);
        packet.payload.putShort(vfr_hud_heading);
        packet.payload.putByte(id);
        packet.payload.putByte(function);
        packet.payload.putByte(type);
        packet.payload.putByte(battery_remaining);
        packet.payload.putByte(fly_status);
        packet.payload.putByte(yaw_move_status);
        packet.payload.putByte(get_fence_status);
        packet.payload.putByte(heartbeat_base_mode);
        packet.payload.putByte(gps_raw_int_satellites_visible);
        packet.payload.putByte(rc_status);
        return packet;
    }

    /**
     * Decode a battery_status message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        this.current_consumed = payload.getInt();
        this.energy_consumed = payload.getInt();
        this.heartbeat_custom_mode = payload.getInt();
        this.vfr_hud_alt = payload.getFloat();
        this.vfr_hud_climb = payload.getFloat();
        this.vfr_hud_groundspeed = payload.getFloat();
        this.local_position_ned_x = payload.getFloat();
        this.local_position_ned_y = payload.getFloat();
        this.local_position_ned_z = payload.getFloat();
        this.local_position_ned_vx = payload.getFloat();
        this.local_position_ned_vy = payload.getFloat();
        this.local_position_ned_vz = payload.getFloat();
        this.attitude_roll = payload.getFloat();
        this.attitude_pitch = payload.getFloat();
        this.temperature = payload.getShort();
        for (int i = 0; i < this.voltages.length; i++) {
            this.voltages[i] = payload.getShort();
        }
        this.current_battery = payload.getShort();
        this.remaining_mah = payload.getShort();
        this.cycle_count = payload.getShort();
        this.full_charge = payload.getShort();
        this.fence_radius_mobile = payload.getShort();
        this.fence_rtl_max_mobile = payload.getShort();
        this.rtl_alt_mobile = payload.getShort();
        this.vfr_hud_heading = payload.getShort();
        this.id = payload.getByte();
        this.function = payload.getByte();
        this.type = payload.getByte();
        this.battery_remaining = payload.getByte();
        this.fly_status = payload.getByte();
        this.yaw_move_status = payload.getByte();
        this.get_fence_status = payload.getByte();
        this.heartbeat_base_mode = payload.getByte();
        this.gps_raw_int_satellites_visible = payload.getByte();
        this.rc_status = payload.getByte();
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_battery_status() {
        msgid = MAVLINK_MSG_ID_BATTERY_STATUS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     */
    public msg_battery_status(MAVLinkPacket mavLinkPacket) {
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_BATTERY_STATUS;

        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "BATTERY_STATUS");
        //Log.d("MAVLINK_MSG_ID_BATTERY_STATUS", toString());
    }


    /**
     * Returns a string with the MSG name and data
     */
    public String toString() {
        return "MAVLINK_MSG_ID_BATTERY_STATUS -"
                + " current_consumed:" + current_consumed
                + " energy_consumed:" + energy_consumed
                + " heartbeat_custom_mode:" + heartbeat_custom_mode
                + " vfr_hud_alt:" + vfr_hud_alt
                + " vfr_hud_climb:" + vfr_hud_climb
                + " vfr_hud_groundspeed:" + vfr_hud_groundspeed
                + " local_position_ned_x:" + local_position_ned_x
                + " local_position_ned_y:" + local_position_ned_y
                + " local_position_ned_z:" + local_position_ned_z
                + " local_position_ned_vx:" + local_position_ned_vx
                + " local_position_ned_vy:" + local_position_ned_vy
                + " local_position_ned_vz:" + local_position_ned_vz
                + " attitude_roll:" + attitude_roll
                + " attitude_pitch:" + attitude_pitch
                + " temperature:" + temperature
                + " voltages:" + voltages
                + " current_battery:" + current_battery
                + "remaining_mah:" + remaining_mah
                + "cycle_count:" + cycle_count
                + "full_charge:" + full_charge
                + "fence_radius_mobile:" + fence_radius_mobile
                + "fence_rtl_max_mobile:" + fence_rtl_max_mobile
                + "rtl_alt_mobile:" + rtl_alt_mobile
                + "vfr_hud_heading:" + vfr_hud_heading
                + " id:" + id
                + " function:" + function
                + " type:" + type
                + " battery_remaining:" + battery_remaining
                + "fly_status:" + fly_status
                + "yaw_move_status:" + yaw_move_status
                + "get_fence_status:" + get_fence_status
                + "heartbeat_base_mode:" + heartbeat_base_mode
                + "gps_raw_int_satellites_visible:" + gps_raw_int_satellites_visible
                + "rc_status:" + rc_status;
    }
}
        