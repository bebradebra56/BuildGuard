package com.buildfgu.guardbiu.grfvd.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication
import com.buildfgu.guardbiu.grfvd.presentation.ui.load.BuildGuardLoadFragment
import org.koin.android.ext.android.inject

class BuildGuardV : Fragment(){

    private lateinit var buildGuardPhoto: Uri
    private var buildGuardFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val buildGuardTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        buildGuardFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        buildGuardFilePathFromChrome = null
    }

    private val buildGuardTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            buildGuardFilePathFromChrome?.onReceiveValue(arrayOf(buildGuardPhoto))
            buildGuardFilePathFromChrome = null
        } else {
            buildGuardFilePathFromChrome?.onReceiveValue(null)
            buildGuardFilePathFromChrome = null
        }
    }

    private val buildGuardDataStore by activityViewModels<BuildGuardDataStore>()


    private val buildGuardViFun by inject<BuildGuardViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (buildGuardDataStore.buildGuardView.canGoBack()) {
                        buildGuardDataStore.buildGuardView.goBack()
                        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "WebView can go back")
                    } else if (buildGuardDataStore.buildGuardViList.size > 1) {
                        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "WebView can`t go back")
                        buildGuardDataStore.buildGuardViList.removeAt(buildGuardDataStore.buildGuardViList.lastIndex)
                        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "WebView list size ${buildGuardDataStore.buildGuardViList.size}")
                        buildGuardDataStore.buildGuardView.destroy()
                        val previousWebView = buildGuardDataStore.buildGuardViList.last()
                        buildGuardAttachWebViewToContainer(previousWebView)
                        buildGuardDataStore.buildGuardView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (buildGuardDataStore.buildGuardIsFirstCreate) {
            buildGuardDataStore.buildGuardIsFirstCreate = false
            buildGuardDataStore.buildGuardContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return buildGuardDataStore.buildGuardContainerView
        } else {
            return buildGuardDataStore.buildGuardContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "onViewCreated")
        if (buildGuardDataStore.buildGuardViList.isEmpty()) {
            buildGuardDataStore.buildGuardView = BuildGuardVi(requireContext(), object :
                BuildGuardCallBack {
                override fun buildGuardHandleCreateWebWindowRequest(buildGuardVi: BuildGuardVi) {
                    buildGuardDataStore.buildGuardViList.add(buildGuardVi)
                    Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "WebView list size = ${buildGuardDataStore.buildGuardViList.size}")
                    Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "CreateWebWindowRequest")
                    buildGuardDataStore.buildGuardView = buildGuardVi
                    buildGuardVi.buildGuardSetFileChooserHandler { callback ->
                        buildGuardHandleFileChooser(callback)
                    }
                    buildGuardAttachWebViewToContainer(buildGuardVi)
                }

            }, buildGuardWindow = requireActivity().window).apply {
                buildGuardSetFileChooserHandler { callback ->
                    buildGuardHandleFileChooser(callback)
                }
            }
            buildGuardDataStore.buildGuardView.buildGuardFLoad(arguments?.getString(
                BuildGuardLoadFragment.BUILD_GUARD_D) ?: "")
//            ejvview.fLoad("www.google.com")
            buildGuardDataStore.buildGuardViList.add(buildGuardDataStore.buildGuardView)
            buildGuardAttachWebViewToContainer(buildGuardDataStore.buildGuardView)
        } else {
            buildGuardDataStore.buildGuardViList.forEach { webView ->
                webView.buildGuardSetFileChooserHandler { callback ->
                    buildGuardHandleFileChooser(callback)
                }
            }
            buildGuardDataStore.buildGuardView = buildGuardDataStore.buildGuardViList.last()

            buildGuardAttachWebViewToContainer(buildGuardDataStore.buildGuardView)
        }
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "WebView list size = ${buildGuardDataStore.buildGuardViList.size}")
    }

    private fun buildGuardHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        buildGuardFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Launching file picker")
                    buildGuardTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Launching camera")
                    buildGuardPhoto = buildGuardViFun.buildGuardSavePhoto()
                    buildGuardTakePhoto.launch(buildGuardPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                buildGuardFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun buildGuardAttachWebViewToContainer(w: BuildGuardVi) {
        buildGuardDataStore.buildGuardContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            buildGuardDataStore.buildGuardContainerView.removeAllViews()
            buildGuardDataStore.buildGuardContainerView.addView(w)
        }
    }


}