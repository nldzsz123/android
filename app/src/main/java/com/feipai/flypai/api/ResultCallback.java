package com.feipai.flypai.api;

import com.feipai.flypai.ui.view.Camera.CameraSetConstaint;

public class ResultCallback {
    public interface IntCallback{
        void onResult(int value);
    }

    public interface BoolCallback{
        void onResult(boolean yesOrNot);
    }

    public interface CameraStatusCallback{
        void onResult(@CameraSetConstaint.CameraStatus int status);
    }
}
