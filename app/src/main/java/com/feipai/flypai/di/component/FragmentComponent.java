package com.feipai.flypai.di.component;

import android.support.v4.app.Fragment;


import com.feipai.flypai.di.module.FragmentModule;
import com.feipai.flypai.di.scope.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {

    Fragment getFragment();

//    void inject(DealerCustomerFragment dealerCustomerFragment);

}
