package com.buildfgu.guardbiu.grfvd.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.buildfgu.guardbiu.MainActivity
import com.buildfgu.guardbiu.R
import com.buildfgu.guardbiu.databinding.FragmentLoadBuildGuardBinding
import com.buildfgu.guardbiu.grfvd.data.shar.BuildGuardSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class BuildGuardLoadFragment : Fragment(R.layout.fragment_load_build_guard) {
    private lateinit var buildGuardLoadBinding: FragmentLoadBuildGuardBinding

    private val buildGuardLoadViewModel by viewModel<BuildGuardLoadViewModel>()

    private val buildGuardSharedPreference by inject<BuildGuardSharedPreference>()

    private var buildGuardUrl = ""

    private val buildGuardRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        buildGuardSharedPreference.buildGuardNotificationState = 2
        buildGuardNavigateToSuccess(buildGuardUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildGuardLoadBinding = FragmentLoadBuildGuardBinding.bind(view)

        buildGuardLoadBinding.buildGuardGrandButton.setOnClickListener {
            val buildGuardPermission = Manifest.permission.POST_NOTIFICATIONS
            buildGuardRequestNotificationPermission.launch(buildGuardPermission)
        }

        buildGuardLoadBinding.buildGuardSkipButton.setOnClickListener {
            buildGuardSharedPreference.buildGuardNotificationState = 1
            buildGuardSharedPreference.buildGuardNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            buildGuardNavigateToSuccess(buildGuardUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                buildGuardLoadViewModel.buildGuardHomeScreenState.collect {
                    when (it) {
                        is BuildGuardLoadViewModel.BuildGuardHomeScreenState.BuildGuardLoading -> {

                        }

                        is BuildGuardLoadViewModel.BuildGuardHomeScreenState.BuildGuardError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is BuildGuardLoadViewModel.BuildGuardHomeScreenState.BuildGuardSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val buildGuardNotificationState = buildGuardSharedPreference.buildGuardNotificationState
                                when (buildGuardNotificationState) {
                                    0 -> {
                                        buildGuardLoadBinding.buildGuardNotiGroup.visibility = View.VISIBLE
                                        buildGuardLoadBinding.buildGuardLoadingGroup.visibility = View.GONE
                                        buildGuardUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > buildGuardSharedPreference.buildGuardNotificationRequest) {
                                            buildGuardLoadBinding.buildGuardNotiGroup.visibility = View.VISIBLE
                                            buildGuardLoadBinding.buildGuardLoadingGroup.visibility = View.GONE
                                            buildGuardUrl = it.data
                                        } else {
                                            buildGuardNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        buildGuardNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                buildGuardNavigateToSuccess(it.data)
                            }
                        }

                        BuildGuardLoadViewModel.BuildGuardHomeScreenState.BuildGuardNotInternet -> {
                            buildGuardLoadBinding.buildGuardStateGroup.visibility = View.VISIBLE
                            buildGuardLoadBinding.buildGuardLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun buildGuardNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_buildGuardLoadFragment_to_buildGuardV,
            bundleOf(BUILD_GUARD_D to data)
        )
    }

    companion object {
        const val BUILD_GUARD_D = "buildGuardData"
    }
}