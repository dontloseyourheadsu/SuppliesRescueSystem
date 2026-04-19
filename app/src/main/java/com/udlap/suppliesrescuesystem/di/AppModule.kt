package com.udlap.suppliesrescuesystem.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udlap.suppliesrescuesystem.data.local.DraftDataStore
import com.udlap.suppliesrescuesystem.data.local.UserDataStore
import com.udlap.suppliesrescuesystem.data.repository.AuthRepositoryImpl
import com.udlap.suppliesrescuesystem.data.repository.RescueRepositoryImpl
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
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
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDraftDataStore(@ApplicationContext context: Context): DraftDataStore = DraftDataStore(context)

    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): UserDataStore = UserDataStore(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideRescueRepository(
        firestore: FirebaseFirestore
    ): RescueRepository = RescueRepositoryImpl(firestore)
}
