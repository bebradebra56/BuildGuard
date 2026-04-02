package com.buildfgu.guardbiu.grfvd

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.buildfgu.guardbiu.grfvd.presentation.app.BuildGuardApplication

class BuildGuardGlobalLayoutUtil {

    private var buildGuardMChildOfContent: View? = null
    private var buildGuardUsableHeightPrevious = 0

    fun buildGuardAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        buildGuardMChildOfContent = content.getChildAt(0)

        buildGuardMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val buildGuardUsableHeightNow = buildGuardComputeUsableHeight()
        if (buildGuardUsableHeightNow != buildGuardUsableHeightPrevious) {
            val buildGuardUsableHeightSansKeyboard = buildGuardMChildOfContent?.rootView?.height ?: 0
            val buildGuardHeightDifference = buildGuardUsableHeightSansKeyboard - buildGuardUsableHeightNow

            if (buildGuardHeightDifference > (buildGuardUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(BuildGuardApplication.buildGuardInputMode)
            } else {
                activity.window.setSoftInputMode(BuildGuardApplication.buildGuardInputMode)
            }
//            mChildOfContent?.requestLayout()
            buildGuardUsableHeightPrevious = buildGuardUsableHeightNow
        }
    }

    private fun buildGuardComputeUsableHeight(): Int {
        val r = Rect()
        buildGuardMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}