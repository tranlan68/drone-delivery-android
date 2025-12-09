package com.locker.uservht.di

import com.locker.uservht.navigation.AppNavigation
import com.locker.uservht.navigation.AppNavigatorImpl
import com.delivery.core.navigationComponent.BaseNavigator
import com.delivery.setting.DemoNavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    @ActivityScoped
    abstract fun provideBaseNavigation(navigation: AppNavigatorImpl): BaseNavigator

    @Binds
    @ActivityScoped
    abstract fun provideAppNavigation(navigation: AppNavigatorImpl): AppNavigation

    @Binds
    @ActivityScoped
    abstract fun provideDemoNavigation(navigation: AppNavigatorImpl): DemoNavigation
}