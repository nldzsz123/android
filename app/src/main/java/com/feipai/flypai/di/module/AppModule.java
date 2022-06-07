package com.feipai.flypai.di.module;

import com.feipai.flypai.api.RetrofitHelper;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.di.scope.ContextLife;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final FlyPieApplication application;

    public AppModule(FlyPieApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    @ContextLife("Application")
    FlyPieApplication provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    RetrofitHelper provideRetrofitHelper() {
        return new RetrofitHelper();
    }

}
