package com.buildfgu.guardbiu.di

import androidx.room.Room
import com.buildfgu.guardbiu.data.local.AppDatabase
import com.buildfgu.guardbiu.data.preferences.UserPreferences
import com.buildfgu.guardbiu.data.repository.AppRepository
import com.buildfgu.guardbiu.ui.screens.calendar.CalendarViewModel
import com.buildfgu.guardbiu.ui.screens.dashboard.DashboardViewModel
import com.buildfgu.guardbiu.ui.screens.furniture.FurniturePlacementViewModel
import com.buildfgu.guardbiu.ui.screens.furniture.FurnitureViewModel
import com.buildfgu.guardbiu.ui.screens.history.HistoryViewModel
import com.buildfgu.guardbiu.ui.screens.layout.LayoutEditorViewModel
import com.buildfgu.guardbiu.ui.screens.materials.MaterialsViewModel
import com.buildfgu.guardbiu.ui.screens.measurements.MeasurementsViewModel
import com.buildfgu.guardbiu.ui.screens.profile.ProfileViewModel
import com.buildfgu.guardbiu.ui.screens.projects.ProjectsViewModel
import com.buildfgu.guardbiu.ui.screens.reports.ReportsViewModel
import com.buildfgu.guardbiu.ui.screens.rooms.RoomsViewModel
import com.buildfgu.guardbiu.ui.screens.scan.ScanViewModel
import com.buildfgu.guardbiu.ui.screens.settings.SettingsViewModel
import com.buildfgu.guardbiu.ui.screens.shopping.ShoppingViewModel
import com.buildfgu.guardbiu.ui.screens.tasks.TasksViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "buildguard_db"
        ).build()
    }

    single { AppRepository(get()) }
    single { UserPreferences(androidContext()) }

    viewModel { DashboardViewModel(get(), get()) }
    viewModel { ProjectsViewModel(get()) }
    viewModel { params -> RoomsViewModel(params.get(), get()) }
    viewModel { params -> ScanViewModel(params.get(), get()) }
    viewModel { params -> LayoutEditorViewModel(params.get(), get()) }
    viewModel { params -> MeasurementsViewModel(params.get(), get()) }
    viewModel { params -> FurnitureViewModel(params.get(), get()) }
    viewModel { params -> FurniturePlacementViewModel(params.get(), get()) }
    viewModel { MaterialsViewModel(get()) }
    viewModel { params -> ShoppingViewModel(params.get(), get()) }
    viewModel { params -> ReportsViewModel(params.get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { TasksViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
