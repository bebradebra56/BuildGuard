package com.buildfgu.guardbiu.grfvd.domain.usecases

import android.util.Log
import com.buildfgu.guardbiu.grfvd.data.repo.BuildGuardRepository
import com.buildfgu.guardbiu.grfvd.data.utils.BuildGuardPushToken
import com.buildfgu.guardbiu.grfvd.data.utils.BuildGuardSystemService
import com.buildfgu.guardbiu.grfvd.domain.model.BuildGuardEntity
import com.buildfgu.guardbiu.grfvd.domain.model.BuildGuardParam
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication

class BuildGuardGetAllUseCase(
    private val buildGuardRepository: BuildGuardRepository,
    private val buildGuardSystemService: BuildGuardSystemService,
    private val buildGuardPushToken: BuildGuardPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : BuildGuardEntity?{
        val params = BuildGuardParam(
            buildGuardLocale = buildGuardSystemService.buildGuardGetLocale(),
            buildGuardPushToken = buildGuardPushToken.buildGuardGetToken(),
            buildGuardAfId = buildGuardSystemService.buildGuardGetAppsflyerId()
        )
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Params for request: $params")
        return buildGuardRepository.buildGuardGetClient(params, conversion)
    }



}