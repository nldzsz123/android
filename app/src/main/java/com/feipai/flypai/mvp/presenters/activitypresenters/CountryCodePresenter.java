package com.feipai.flypai.mvp.presenters.activitypresenters;

import android.os.Bundle;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.activitycontract.CountryCodeContract;
import com.feipai.flypai.mvp.contract.activitycontract.CountryCodeContract.View;

import static me.yokeyword.fragmentation.ISupportFragment.RESULT_OK;

public class CountryCodePresenter implements CountryCodeContract.Presenter, BasePresenter<View> {
    private View mView;

    @Override
    public void attachView(View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }

    @Override
    public void countryItemSelect(BaseMvpFragment ac, String country, String countryCode) {
        Bundle bundle = new Bundle();
        bundle.putString(ConstantFields.RESULT_PARAM.COUNTRY_KEY, country);
        bundle.putString(ConstantFields.RESULT_PARAM.COUNTRY_CODE_KEY, countryCode);
        ac.setFragmentResult(RESULT_OK, bundle);
        ac.pop();
    }

    public void finishCountryCodePage(BaseMvpFragment ac){
        ac.setFragmentResult(RESULT_OK, null);
        ac.pop();
    }
}
