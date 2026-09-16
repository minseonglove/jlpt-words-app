package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.ui.adzero.AdZeroViewModel
import com.minseonglove.jlptwords.ui.home.HomeViewModel
import com.minseonglove.jlptwords.ui.main.MainViewModel
import com.minseonglove.jlptwords.ui.search.SearchViewModel
import com.minseonglove.jlptwords.ui.selection.level.LevelSelectionViewModel
import com.minseonglove.jlptwords.ui.selection.session.SessionSelectionViewModel
import com.minseonglove.jlptwords.ui.setting.SettingViewModel
import com.minseonglove.jlptwords.ui.splash.SplashViewModel
import com.minseonglove.jlptwords.ui.study.StudyViewModel
import com.minseonglove.jlptwords.ui.worddetail.WordDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModelOf(::MainViewModel)
        viewModelOf(::SplashViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::SettingViewModel)
        viewModelOf(::AdZeroViewModel)
        viewModelOf(::LevelSelectionViewModel)
        viewModelOf(::SessionSelectionViewModel)
        viewModelOf(::StudyViewModel)
        viewModelOf(::WordDetailViewModel)
        viewModelOf(::SearchViewModel)
    }
