package com.sweak.qralarm.features.optimization.di

import android.app.Application
import android.content.Context
import android.os.PowerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object OptimizationModule {

    @Provides
    @ViewModelScoped
    fun providePowerManager(app: Application): PowerManager =
        app.getSystemService(Context.POWER_SERVICE) as PowerManager

    @Provides
    @ViewModelScoped
    @Named("PackageName")
    fun providePackageName(app: Application): String = app.packageName
}