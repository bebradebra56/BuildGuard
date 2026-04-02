package com.buildfgu.guardbiu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.buildfgu.guardbiu.grfvd.BuildGuardGlobalLayoutUtil
import com.buildfgu.guardbiu.grfvd.buildGuardSetupSystemBars
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication
import com.buildfgu.guardbiu.grfvd.presentation.pushhandler.BuildGuardPushHandler
import org.koin.android.ext.android.inject

class BuildGuardActivity : AppCompatActivity() {

    private val buildGuardPushHandler by inject<BuildGuardPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildGuardSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_build_guard)

        val buildGuardRootView = findViewById<View>(android.R.id.content)
        BuildGuardGlobalLayoutUtil().buildGuardAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(buildGuardRootView) { buildGuardView, buildGuardInsets ->
            val buildGuardSystemBars = buildGuardInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val buildGuardDisplayCutout = buildGuardInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val buildGuardIme = buildGuardInsets.getInsets(WindowInsetsCompat.Type.ime())


            val buildGuardTopPadding = maxOf(buildGuardSystemBars.top, buildGuardDisplayCutout.top)
            val buildGuardLeftPadding = maxOf(buildGuardSystemBars.left, buildGuardDisplayCutout.left)
            val buildGuardRightPadding = maxOf(buildGuardSystemBars.right, buildGuardDisplayCutout.right)
            window.setSoftInputMode(BuildGuardApplication.buildGuardInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "ADJUST PUN")
                val buildGuardBottomInset = maxOf(buildGuardSystemBars.bottom, buildGuardDisplayCutout.bottom)

                buildGuardView.setPadding(buildGuardLeftPadding, buildGuardTopPadding, buildGuardRightPadding, 0)

                buildGuardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = buildGuardBottomInset
                }
            } else {
                Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "ADJUST RESIZE")

                val buildGuardBottomInset = maxOf(buildGuardSystemBars.bottom, buildGuardDisplayCutout.bottom, buildGuardIme.bottom)

                buildGuardView.setPadding(buildGuardLeftPadding, buildGuardTopPadding, buildGuardRightPadding, 0)

                buildGuardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = buildGuardBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(BuildGuardApplication.BUILD_GUARD_MAIN_TAG, "Activity onCreate()")
        buildGuardPushHandler.buildGuardHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            buildGuardSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        buildGuardSetupSystemBars()
    }
}