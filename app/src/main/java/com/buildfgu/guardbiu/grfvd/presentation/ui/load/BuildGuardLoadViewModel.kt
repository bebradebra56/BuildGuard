package com.buildfgu.guardbiu.grfvd.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildfgu.guardbiu.grfvd.data.shar.BuildGuardSharedPreference
import com.buildfgu.guardbiu.grfvd.data.utils.BuildGuardSystemService
import com.buildfgu.guardbiu.grfvd.domain.usecases.BuildGuardGetAllUseCase
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardAppsFlyerState
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuildGuardLoadViewModel(
    private val buildGuardGetAllUseCase: BuildGuardGetAllUseCase,
    private val buildGuardSharedPreference: BuildGuardSharedPreference,
    private val buildGuardSystemService: BuildGuardSystemService
) : ViewModel() {

    private val _buildGuardHomeScreenState: MutableStateFlow<BuildGuardHomeScreenState> =
        MutableStateFlow(BuildGuardHomeScreenState.BuildGuardLoading)
    val buildGuardHomeScreenState = _buildGuardHomeScreenState.asStateFlow()

    private var buildGuardGetApps = false


    init {
        viewModelScope.launch {
            when (buildGuardSharedPreference.buildGuardAppState) {
                0 -> {
                    if (buildGuardSystemService.buildGuardIsOnline()) {
                        BuildGuardApplication.buildGuardConversionFlow.collect {
                            when(it) {
                                BuildGuardAppsFlyerState.BuildGuardDefault -> {}
                                BuildGuardAppsFlyerState.BuildGuardError -> {
                                    buildGuardSharedPreference.buildGuardAppState = 2
                                    _buildGuardHomeScreenState.value =
                                        BuildGuardHomeScreenState.BuildGuardError
                                    buildGuardGetApps = true
                                }
                                is BuildGuardAppsFlyerState.BuildGuardSuccess -> {
                                    if (!buildGuardGetApps) {
                                        buildGuardGetData(it.buildGuardData)
                                        buildGuardGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _buildGuardHomeScreenState.value =
                            BuildGuardHomeScreenState.BuildGuardNotInternet
                    }
                }
                1 -> {
                    if (buildGuardSystemService.buildGuardIsOnline()) {
                        if (BuildGuardApplication.BUILD_GUARD_FB_LI != null) {
                            _buildGuardHomeScreenState.value =
                                BuildGuardHomeScreenState.BuildGuardSuccess(
                                    BuildGuardApplication.BUILD_GUARD_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > buildGuardSharedPreference.buildGuardExpired) {
                            Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Current time more then expired, repeat request")
                            BuildGuardApplication.buildGuardConversionFlow.collect {
                                when(it) {
                                    BuildGuardAppsFlyerState.BuildGuardDefault -> {}
                                    BuildGuardAppsFlyerState.BuildGuardError -> {
                                        _buildGuardHomeScreenState.value =
                                            BuildGuardHomeScreenState.BuildGuardSuccess(
                                                buildGuardSharedPreference.buildGuardSavedUrl
                                            )
                                        buildGuardGetApps = true
                                    }
                                    is BuildGuardAppsFlyerState.BuildGuardSuccess -> {
                                        if (!buildGuardGetApps) {
                                            buildGuardGetData(it.buildGuardData)
                                            buildGuardGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Current time less then expired, use saved url")
                            _buildGuardHomeScreenState.value =
                                BuildGuardHomeScreenState.BuildGuardSuccess(
                                    buildGuardSharedPreference.buildGuardSavedUrl
                                )
                        }
                    } else {
                        _buildGuardHomeScreenState.value =
                            BuildGuardHomeScreenState.BuildGuardNotInternet
                    }
                }
                2 -> {
                    _buildGuardHomeScreenState.value =
                        BuildGuardHomeScreenState.BuildGuardError
                }
            }
        }
    }


    private suspend fun buildGuardGetData(conversation: MutableMap<String, Any>?) {
        val buildGuardData = buildGuardGetAllUseCase.invoke(conversation)
        if (buildGuardSharedPreference.buildGuardAppState == 0) {
            if (buildGuardData == null) {
                buildGuardSharedPreference.buildGuardAppState = 2
                _buildGuardHomeScreenState.value =
                    BuildGuardHomeScreenState.BuildGuardError
            } else {
                buildGuardSharedPreference.buildGuardAppState = 1
                buildGuardSharedPreference.apply {
                    buildGuardExpired = buildGuardData.buildGuardExpires
                    buildGuardSavedUrl = buildGuardData.buildGuardUrl
                }
                _buildGuardHomeScreenState.value =
                    BuildGuardHomeScreenState.BuildGuardSuccess(buildGuardData.buildGuardUrl)
            }
        } else  {
            if (buildGuardData == null) {
                _buildGuardHomeScreenState.value =
                    BuildGuardHomeScreenState.BuildGuardSuccess(
                        buildGuardSharedPreference.buildGuardSavedUrl
                    )
            } else {
                buildGuardSharedPreference.apply {
                    buildGuardExpired = buildGuardData.buildGuardExpires
                    buildGuardSavedUrl = buildGuardData.buildGuardUrl
                }
                _buildGuardHomeScreenState.value =
                    BuildGuardHomeScreenState.BuildGuardSuccess(buildGuardData.buildGuardUrl)
            }
        }
    }


    sealed class BuildGuardHomeScreenState {
        data object BuildGuardLoading : BuildGuardHomeScreenState()
        data object BuildGuardError : BuildGuardHomeScreenState()
        data class BuildGuardSuccess(val data: String) : BuildGuardHomeScreenState()
        data object BuildGuardNotInternet: BuildGuardHomeScreenState()
    }
}