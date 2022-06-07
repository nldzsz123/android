package com.feipai.flypai.di.component;

import android.app.Activity;

import com.feipai.flypai.di.module.ActivityModule;
import com.feipai.flypai.di.scope.ActivityScope;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    Activity getActivity();

//    void inject(UserLoginActivity userLoginActivity);

}