package com.sweak.qralarm.features.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.File

@Module
@InstallIn(ViewModelComponent::class)
object FeaturesModule {

    @Provides
    @ViewModelScoped
    fun provideFilesDir(
        @ApplicationContext context: Context
    ): File = context.filesDir
}