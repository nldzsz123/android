package com.feipai.flypai.beans;

import android.util.ArrayMap;

import com.feipai.flypai.base.BaseEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by YangLin on 2016-09-20.
 */

public class ABCmdValue<T> extends BaseEntity {


    /**
     * rval : 0
     * msg_id : 9
     * permission : settable
     * param : photo_size
     * options : ["16M (4608x3456 4:3)","12M (4608x2592 16:9)"]
     * listing:
     */

    private int rval;
    private int msg_id;
    private String permission;
    private T param;
    private List<String> options;
    private String type;
    private List<T> listing;
    private String md5sum;
    private String size;
    private String date;
    private String resolution;
    private int duration;
    private String media_type;
//    private List<Map<String, String>> list;


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public int getRval() {
        if (rval == -21)
            rval = 0;
        return rval;
    }

    public void setRval(int rval) {
        if (rval == -21)
            rval = 0;
        this.rval = rval;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }


    public List<T> getListing() {
        return listing;
    }

    public void getListing(List<T> listing) {
        this.listing = listing;
    }

//    public String getListing() {
//        return listing;
//    }
//
//    public void setListing(String listing) {
//        this.listing = listing;
//    }

    //    public void setList(List<Map<String, String>> list) {
//        this.list = list;
//    }
//
    public List<Map<String, String>> getList() {
        try {
            JSONArray jarr = new JSONArray(getListing());
            List<Map<String, String>> listMap = new ArrayList<>();
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jsonObject = jarr.getJSONObject(i);
                for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                    String key = iterator.next();
                    Map<String, String> map = new ArrayMap<>();
//                    System.out.println(key);
                    String value = jsonObject.getString(key);
//                    System.out.println(value);
                    map.put(key, value);
                    listMap.add(map);
                }
            }
            return listMap;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMd5(List<T> parmam) {
        String md5 = null;
        if (parmam != null && !parmam.equals("")) {
            try {
                JSONArray jarr = new JSONArray(parmam);
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject jsonObject = jarr.getJSONObject(i);
                    for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                        String key = iterator.next();
                        if (key.equals("md5sum")) {
                            md5 = jsonObject.getString(key);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    @Override
    public String toString() {
        String result = "ABCmdValue{";
        result += "rval=" + rval;
        result += ", msg_id=" + msg_id;
        if (permission != null) {
            result += ", permission='" + permission + '\'';
        }
        if (param != null) {
            result += ", param='" + param + '\'';
        }
        if (type != null) {
            result += ", type='" + type + '\'';
        }
        if (options != null) {
            result += ", options=" + options;
        }
        if (listing != null) {
            result += ", listing=" + listing;
        }
        if (date != null) {
            result += ", date=" + date;
        }
        if (resolution != null) {
            result += ", resolution=" + resolution;
        }
        if (duration != 0) {
            result += ", duration=" + duration;
        }
        if (media_type != null) {
            result += ", media_type=" + media_type;
        }
        if (size != null) {
            result += ",size" + size;
        }
        result += '}';

        return result;
    }
}
