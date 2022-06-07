package com.feipai.flypai.di.module;

import android.support.v4.app.Fragment;

import com.feipai.flypai.di.scope.FragmentScope;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private Fragment mFragment;

    public FragmentModule(Fragment fragment) {
        this.mFragment = fragment;
    }

    @Provides
    @FragmentScope
    public Fragment provideFragment() {
        return this.mFragment;
    }

}
