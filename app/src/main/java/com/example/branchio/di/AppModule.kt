package com.example.branchio.di

import com.example.branchio.core.viewmodel.AppViewModel
import com.example.branchio.domain.repository.BranchRepository
import com.example.branchio.domain.repository.BranchRepositoryImpl
import com.example.branchio.domain.usecases.GenerateBranchLinkUseCase
import com.example.branchio.domain.usecases.HandleDeepLinkUseCase
import com.example.branchio.domain.usecases.InitBranchSessionUseCase
import com.example.branchio.domain.usecases.ReinitBranchSessionUseCase
import com.example.branchio.features.deepLink.DeepLinkViewModel
import com.example.branchio.features.home.viewModels.HomeScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {


    // Repository
    single<BranchRepository> {
        BranchRepositoryImpl(get())
    }

    // UseCase
    single {
        GenerateBranchLinkUseCase(get())
    }
    single { InitBranchSessionUseCase(get()) }
    single { ReinitBranchSessionUseCase(get()) }
    single { HandleDeepLinkUseCase(get()) }

    // ViewModel
    viewModel {
        HomeScreenViewModel(generateBranchLinkUseCase = get())
    }
    viewModel{
        AppViewModel(initBranchSessionUseCase = get(), reinitBranchSessionUseCase = get())
    }
    viewModel{
        DeepLinkViewModel(handleDeepLinkUseCase = get())
    }


}