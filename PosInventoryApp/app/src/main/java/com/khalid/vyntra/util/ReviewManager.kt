package com.khalid.vyntra.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewManager {
    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }
}
