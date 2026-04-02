package com.buildfgu.guardbiu.grfvd.presentation.di

import com.buildfgu.guardbiu.grfvd.data.repo.BuildGuardRepository
import com.buildfgu.guardbiu.grfvd.data.shar.BuildGuardSharedPreference
import com.buildfgu.guardbiu.grfvd.data.utils.BuildGuardPushToken
import com.buildfgu.guardbiu.grfvd.data.utils.BuildGuardSystemService
import com.buildfgu.guardbiu.grfvd.domain.usecases.BuildGuardGetAllUseCase
import com.buildfgu.guardbiu.grfvd.presentation.pushhandler.BuildGuardPushHandler
import com.buildfgu.guardbiu.grfvd.presentation.ui.load.BuildGuardLoadViewModel
import com.buildfgu.guardbiu.grfvd.presentation.ui.view.BuildGuardViFun
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val buildGuardModule = module {
    factory {
        BuildGuardPushHandler()
    }
    single {
        BuildGuardRepository()
    }
    single {
        BuildGuardSharedPreference(get())
    }
    factory {
        BuildGuardPushToken()
    }
    factory {
        BuildGuardSystemService(get())
    }
    factory {
        BuildGuardGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        BuildGuardViFun(get())
    }
    viewModel {
        BuildGuardLoadViewModel(get(), get(), get())
    }
}