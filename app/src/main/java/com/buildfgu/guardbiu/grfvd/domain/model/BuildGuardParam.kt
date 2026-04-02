package com.buildfgu.guardbiu.grfvd.domain.model

import com.google.gson.annotations.SerializedName


private const val BUILD_GUARD_A = "com.buildfgu.guardbiu"
private const val BUILD_GUARD_B = "buildguard-44492"
data class BuildGuardParam (
    @SerializedName("af_id")
    val buildGuardAfId: String,
    @SerializedName("bundle_id")
    val buildGuardBundleId: String = BUILD_GUARD_A,
    @SerializedName("os")
    val buildGuardOs: String = "Android",
    @SerializedName("store_id")
    val buildGuardStoreId: String = BUILD_GUARD_A,
    @SerializedName("locale")
    val buildGuardLocale: String,
    @SerializedName("push_token")
    val buildGuardPushToken: String,
    @SerializedName("firebase_project_id")
    val buildGuardFirebaseProjectId: String = BUILD_GUARD_B,

    )