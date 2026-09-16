package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.ad.InterstitialAdManager
import com.minseonglove.jlptwords.ad.InterstitialAdManagerImpl
import com.minseonglove.jlptwords.ad.NativeAdManager
import com.minseonglove.jlptwords.ad.NativeAdManagerImpl
import com.minseonglove.jlptwords.ad.RewardedAdManager
import com.minseonglove.jlptwords.ad.RewardedAdManagerImpl
import com.minseonglove.jlptwords.ad.TrackingConsentManager
import com.minseonglove.jlptwords.ad.TrackingConsentManagerImpl
import org.koin.dsl.module

actual val adModule =
    module {
        single<InterstitialAdManager> { InterstitialAdManagerImpl(get()) }
        single<NativeAdManager> { NativeAdManagerImpl(get()) }
        single<RewardedAdManager> { RewardedAdManagerImpl(get()) }
        single<TrackingConsentManager> { TrackingConsentManagerImpl() }
    }
