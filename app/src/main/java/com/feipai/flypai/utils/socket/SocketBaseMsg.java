package com.feipai.flypai.utils.socket;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SocketBaseMsg {
    public static final int Result_Unconnected = 0;
    public static final int Result_Ok = 1;
    public static final int Result_Error = 2;
    @IntDef({Result_Unconnected,Result_Ok,Result_Error})
    @Retention(RetentionPolicy.SOURCE)
    @interface result_code{}

    public interface Callback {
        void onComplete(@result_code int result_code,byte[] responseData);
    }

    public int msgTag;
    public int timeout;
    public byte[] params;
    public Callback callback;

    public interface ReturnCallback {
        void onComplete(String data);
    }
    public ReturnCallback returnCallback;

    public SocketBaseMsg() {
        msgTag = 0;
        timeout = 1500;
    }

    public SocketBaseMsg(int msgTag, int timeout, byte[] params,Callback cb) {
        this.msgTag = msgTag;
        this.timeout = timeout;
        this.params = params;
        this.callback = cb;
    }

}
