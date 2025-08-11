package com.example.locationtracking.di

import android.content.Context
import com.example.locationtracking.data.LocalFileDataSource
import com.example.locationtracking.data.LocationRepositoryImpl
import com.example.locationtracking.domain.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext ctx: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(ctx)
    }

    @Provides
    @Singleton
    fun provideLocalFileDataSource(@ApplicationContext ctx: Context): LocalFileDataSource {
        return LocalFileDataSource(ctx.filesDir)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        fused: FusedLocationProviderClient,
        fileDataSource: LocalFileDataSource
    ): LocationRepository {
        return LocationRepositoryImpl(fused, fileDataSource)
    }
}
