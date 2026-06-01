package org.example.srpfe.di

import org.example.srpfe.screens.physicallist.PhysicalListViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        factory { PhysicalListViewModel(get()) }
    }
