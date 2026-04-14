package com.udlap.suppliesrescuesystem.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udlap.suppliesrescuesystem.data.repository.AuthRepositoryImpl
import com.udlap.suppliesrescuesystem.data.repository.RescueRepositoryImpl
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module using Hilt.
 *
 * This module provides singleton instances of Firebase services and repository implementations
 * to be injected into ViewModels and UseCases across the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the singleton instance of [FirebaseAuth].
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Provides the singleton instance of [FirebaseFirestore].
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Binds the [AuthRepository] interface to its implementation [AuthRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore)

    /**
     * Binds the [RescueRepository] interface to its implementation [RescueRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideRescueRepository(
        firestore: FirebaseFirestore
    ): RescueRepository = RescueRepositoryImpl(firestore)
}

