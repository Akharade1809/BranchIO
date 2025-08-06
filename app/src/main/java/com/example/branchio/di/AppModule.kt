package com.example.branchio.di

import com.example.branchio.core.viewmodel.AppViewModel
import com.example.branchio.data.remote.HttpClientFactory
import com.example.branchio.data.remote.RemoteDataSource
import com.example.branchio.data.remote.RemoteDataSourceImpl
import com.example.branchio.domain.repository.BranchRepository
import com.example.branchio.domain.repository.BranchRepositoryImpl
import com.example.branchio.domain.repository.ProductRepository
import com.example.branchio.domain.repository.ProductRepositoryImpl
import com.example.branchio.domain.usecases.GenerateBranchLinkUseCase
import com.example.branchio.domain.usecases.GetAllProductsUseCase
import com.example.branchio.domain.usecases.GetProductByIdUseCase
import com.example.branchio.domain.usecases.InitBranchSessionUseCase
import com.example.branchio.domain.usecases.ReinitBranchSessionUseCase
import com.example.branchio.domain.usecases.TrackContentViewUseCase
import com.example.branchio.domain.usecases.TrackCustomEventUseCase
import com.example.branchio.features.productDetailScreen.ProductDetailViewModel
import com.example.branchio.features.productListScreen.ProductListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    //HttpClient
    single { HttpClientFactory.create() }

    //Datasource
    single<RemoteDataSource> { RemoteDataSourceImpl(get()) }

    // Repository
    single<BranchRepository> { BranchRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }

    // UseCase
    single { GenerateBranchLinkUseCase(get()) }
    single { InitBranchSessionUseCase(get()) }
    single { ReinitBranchSessionUseCase(get()) }
    single { TrackCustomEventUseCase(get()) }
    single { TrackContentViewUseCase(get()) }

    single { GetAllProductsUseCase(get()) }
    single { GetProductByIdUseCase(get()) }

    // ViewModel
    viewModel { AppViewModel(get(), get()) }
    viewModel { ProductListViewModel(get(),get()) }
    viewModel { ProductDetailViewModel(get(), get(), get(), get()) }



}