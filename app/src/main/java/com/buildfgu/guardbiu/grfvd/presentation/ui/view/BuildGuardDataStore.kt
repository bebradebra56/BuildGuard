package com.buildfgu.guardbiu.grfvd.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class BuildGuardDataStore : ViewModel(){
    val buildGuardViList: MutableList<BuildGuardVi> = mutableListOf()
    var buildGuardIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var buildGuardContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var buildGuardView: BuildGuardVi

}