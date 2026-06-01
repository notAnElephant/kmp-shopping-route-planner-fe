package org.example.srpfe.di

import org.example.ApiRepository
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.repository.DefaultApiRepository
import org.example.srpfe.screens.camera.CameraViewModel
import org.example.srpfe.screens.sales.SalesViewModel
import org.example.srpfe.screens.shopmapdrawer.ShopMapDrawerViewModel
import org.example.srpfe.screens.shoppinglist.ShoppingListViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single { AuthSession() }
        single<ApiRepository> { DefaultApiRepository(get()) }
        viewModel { CameraViewModel(get()) }
        viewModel { ShoppingListViewModel(get()) }
        viewModel { ShopMapDrawerViewModel(get()) }
        viewModel { SalesViewModel(get()) }
    }

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(appModule, platformModule())
    }
}

expect fun platformModule(): Module
