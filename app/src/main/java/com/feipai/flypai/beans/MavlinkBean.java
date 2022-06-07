package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class MavlinkBean extends BaseEntity {

    public enum Status {
        SUCCESS("0"), FAILURE("error");
        private String stauts = "0";

        Status(String stauts) {
            this.stauts = stauts;
        }

        public String getStauts() {
            return this.stauts;
        }
    }

    private String mData;

    public String getCmd() {
        return subStr(mData, "<Cmd>", "</Cmd>");
    }

    public String getStatus() {
        return subStr(mData, "<Status>", "</Status>");
    }


    private String subStr(String str, String start, String end) {
        String substring = null;
        try {
            int indexOf = str.indexOf(start);
            if (indexOf > 0) {
                indexOf += start.length();
                int indexOf2 = str.indexOf(end, indexOf);
                if (indexOf2 > indexOf) {
                    substring = str.substring(indexOf, indexOf2);
                    return substring;
                }
            }

            return "error";

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (substring == null)
            return "error";
        return substring;
    }
}
