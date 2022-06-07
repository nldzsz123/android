package com.feipai.flypai.mvp.contract.fragcontract;

import com.feipai.flypai.base.BaseSimpleFragment;
import com.feipai.flypai.mvp.BaseView;


public interface LoginFragContract {


    interface View extends BaseView {
        /**
         * 验证码倒计时
         */
        void startVeriCodeTimer();

        /**
         * 显示loading
         */
        void showHintDialog(String text);

        /**
         * 隐藏loading
         */
        void hideHintDialog();

        /**
         * 吐司
         */
        void showToast(String text);

    }

    interface Presenter {
        /**
         * 登陆
         *
         * @param countryCode
         * @param phoneNumb
         * @param veriCode
         */
        void login(BaseSimpleFragment activity, String countryCode, String phoneNumb, String veriCode);


        /**
         * 读取用户协议
         */
        void readUserAgreement();

        /**
         * 确认用户协议
         */
        void makeSureUserAgreement();


        void chooseCountryCode(BaseSimpleFragment ac);

        /**
         * 获取验证码
         */
        void getPhoneVerificationCode(String countryCode, String photoNumb);
    }
}
