package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.view.Gravity;

import com.amap.api.maps.model.LatLng;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.zhy.autolayout.utils.AutoUtils;

/**
 * 智能功能管理类
 */
public class IPMHelper {
    private TakingPanorDialog mTakingPanorD;
    private AroundDialog mAroundD;
    private DelayTimeDialog mDelayTimeD;
    private WayPointDialog mWayPointD;
    private FollowSnapDialog mFollowD;
    private Context mContext;
    private boolean mIsWide;

    private float waypointSpeed = 0;

    public IPMHelper(Context context) {
        this.mContext = context;
    }

    /**
     * 创建全景对话框
     *
     * @param isWide 是否是广角
     **/
    public IPMHelper createTakingPanorDg(boolean isWide, TakingPanorDialog.OnClickCallback callback) {
        this.mIsWide = isWide;
        if (mTakingPanorD == null) {
            mTakingPanorD = new TakingPanorDialog(mContext)
                    .setCallback(callback)
                    .onCreate(Gravity.TOP | Gravity.LEFT, AutoUtils.getPercentWidthSize(16)
                            , AutoUtils.getPercentHeightSize(104));

        }
        mTakingPanorD.setWide(mIsWide);
        return this;
    }

    public boolean isTakingPanorDShowing() {
        return mTakingPanorD != null && mTakingPanorD.isShowing();
    }


    /**
     * 显示全景拍摄对话框
     */
    public void showTakingPanorD() {
        if (!isTakingPanorDShowing())
            mTakingPanorD.show();
    }

    public void dismissTakingPanorD(int where) {
//        ToastUtils.showLongToast("对话框消失" + where);
        if (isTakingPanorDShowing()) {
            mTakingPanorD.dismiss();
        }
    }


    /**
     * 主要用于更新已拍摄到多少张
     */
    public void updateTakingPanorD(int count) {
        if (mTakingPanorD != null && mTakingPanorD.isShowing()) {
            mTakingPanorD.updateCountForPanor(count);
        }
    }


    /**
     * 创建around对话框
     */
    public IPMHelper createAroundD(AroundDialog.OnClickCallback callback) {
        if (mAroundD == null) {
            mAroundD = new AroundDialog(mContext)
                    .onCreate(Gravity.TOP | Gravity.LEFT, AutoUtils.getPercentWidthSize(16)
                            , AutoUtils.getPercentHeightSize(104))
                    .setCallback(callback);

        }
        return this;
    }

    public boolean isAroundDShowing() {
        return mAroundD != null && mAroundD.isShowing();
    }

    /**
     * 显示环绕对话框
     */
    public void showAroundD() {
        LogUtils.d("显示环绕飞行对话框");
        if (!isAroundDShowing())
            mAroundD.show();
    }

    public void dismissAroundD() {
        if (isAroundDShowing())
            mAroundD.dismiss();
    }


    public void updateAroundR(double radius) {
        if (isAroundDShowing()) {
            mAroundD.setArounRadius(radius);
        }
    }

    public void updateAroundH(String height) {
        if (isAroundDShowing()) {
            mAroundD.setAroundHeight(height);
        }
    }


    /**
     * 创建延时拍摄
     */
    public IPMHelper createDelayTimeD(DelayTimeDialog.OnEventCallback callback) {
        if (mDelayTimeD == null)
            mDelayTimeD = new DelayTimeDialog(mContext)
                    .onCreate(Gravity.TOP | Gravity.LEFT, AutoUtils.getPercentWidthSize(16)
                            , AutoUtils.getPercentHeightSize(104))
                    .setCallback(callback);
        return this;
    }

    public boolean isDelayTimeDShowing() {
        return mDelayTimeD != null && mDelayTimeD.isShowing();
    }


    /**
     * 显示延时拍摄对话框
     */
    public void showDelayTimeD() {
        LogUtils.d("显示移动延时对话框");
        if (!isDelayTimeDShowing())
            mDelayTimeD.show();
    }

    public void dismissDelayTimeD() {
        if (isDelayTimeDShowing()) {
            mDelayTimeD.dismiss();
        }
    }

    public void updateDelayTimeD(int count) {
        if (isDelayTimeDShowing())
            mDelayTimeD.updeCount(count);
    }

    public void updateDelayDistance(String distance) {
        if (isDelayTimeDShowing()) {
            mDelayTimeD.updateDelayTimeDistance(distance);
        }
    }

    /**
     * 创建航点
     */
    public IPMHelper createWaypointD(WayPointDialog.OnEventCallback onEventCallback) {
        if (mWayPointD == null)
            mWayPointD = new WayPointDialog(mContext)
                    .onCreate(Gravity.TOP | Gravity.LEFT, AutoUtils.getPercentWidthSize(16)
                            , AutoUtils.getPercentHeightSize(104))
                    .setCallback(onEventCallback);
        return this;
    }


    public boolean isWaypointDShowing() {
        return mWayPointD != null && mWayPointD.isShowing();
    }

    /**
     * 显示航点对话框
     */
    public void showWaypointD() {
        if (!isWaypointDShowing())
            mWayPointD.show();
    }


    public void dismissWaypointD() {
        if (isWaypointDShowing()) {
            mWayPointD.dismiss();
        }
    }

    public boolean isWaypointSetPoints() {
        return mWayPointD != null && isWaypointDShowing() && mWayPointD.isWaypointSet();
    }

    /**
     * 更新航点个数和总里程
     */
    public void updateWayponitD(int pointsSize, String totalDistance) {
        if (isWaypointDShowing())
            mWayPointD.updateTotalPoints(pointsSize, totalDistance);
    }


    /**
     * 更新航点速度
     */
    public void setWaypointSpeed(int wayPointSpeed) {
        this.waypointSpeed = wayPointSpeed;
        if (isWaypointDShowing()) {
            mWayPointD.setWaypointSpeed(wayPointSpeed);
        }
//        if (isDelayTimeDShowing()) {
//            mDelayTimeD.setWaypointSpees(wayPointSpeed / 100);
//        }
    }


    public IPMHelper createFollowD(FollowSnapDialog.OnEventCallback callback) {
        if (mFollowD == null)
            mFollowD = new FollowSnapDialog(mContext)
                    .onCreate(Gravity.TOP | Gravity.LEFT, AutoUtils.getPercentWidthSize(16)
                            , AutoUtils.getPercentHeightSize(104))
                    .setCallback(callback);
        return this;
    }

    public boolean isFollowDShowing() {
        return mFollowD != null && mFollowD.isShowing();
    }


    public void showFollowD() {
        if (!isFollowDShowing()) {
            mFollowD.show();
        }
    }


    public void dismissFollowD() {
        if (isFollowDShowing()) {
            mFollowD.dismiss();
        }
    }

    public void updateFollowDHeight(String alt) {
        if (isFollowDShowing()) {
            mFollowD.updateFollowHeight(alt);
        }
    }


    public void updateFollowDGps(float mobileGPS, LatLng mobileLatlng, float distance) {
        if (isFollowDShowing()) {
            mFollowD.updateGpsValue(mobileGPS, mobileLatlng, distance);
        }
    }

}
