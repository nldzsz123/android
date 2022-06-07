package com.feipai.flypai.di.component;

import com.feipai.flypai.api.RetrofitHelper;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.di.module.AppModule;
import com.feipai.flypai.di.scope.ContextLife;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    @ContextLife("Application")
    FlyPieApplication getContext();

    RetrofitHelper retrofitHelper();

}