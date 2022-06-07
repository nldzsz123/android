package com.feipai.flypai.mvp.presenters.fragmentpresenters;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import com.feipai.flypai.R;
import com.feipai.flypai.api.RetrofitHelper;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.fragcontract.UserInfoEidtorFragContract;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class UserInfoEidtorPresenter implements UserInfoEidtorFragContract.Presenter, BasePresenter<UserInfoEidtorFragContract.View> {

    private RetrofitHelper mRetrofitHelper;
    private UserInfoEidtorFragContract.View mView;

    @Inject
    public UserInfoEidtorPresenter(RetrofitHelper mRetrofitHelper) {
        this.mRetrofitHelper = mRetrofitHelper;
    }

    @Override
    public void attachView(UserInfoEidtorFragContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }


    @Override
    public void selectHead() {
        List<String> list = new ArrayList<>();
        list.add(mView.getPageActivity().getResources().getString(R.string.take_photo));
        list.add(mView.getPageActivity().getResources().getString(R.string.photo_album));
        mView.showButtomDialog(list);

    }

    @Override
    public void seleCountry() {

    }

    @Override
    public void reposition() {

    }

    @Override
    public void back() {

    }

    @Override
    public void skipToNext() {

    }

    @Override
    public void saveInfo() {

    }

    @Override
    public void getIconFromPhotoAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, ConstantFields.RESULT_PARAM.PHOTO_PICKED_FROM_FILE);
    }

    @Override
    public void getIconFromCamera() {

    }
}
