package com.buildfgu.guardbiu.grfvd.domain.model

import com.google.gson.annotations.SerializedName


data class BuildGuardEntity (
    @SerializedName("ok")
    val buildGuardOk: String,
    @SerializedName("url")
    val buildGuardUrl: String,
    @SerializedName("expires")
    val buildGuardExpires: Long,
)