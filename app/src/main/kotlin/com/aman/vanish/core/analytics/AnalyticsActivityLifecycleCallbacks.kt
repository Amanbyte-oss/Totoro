package com.aman.vanish.core.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Automatically tracks screen views in Firebase Analytics when activities are resumed.
 */
@Singleton
class AnalyticsActivityLifecycleCallbacks @Inject constructor(
	private val analyticsHelper: AnalyticsHelper
) : Application.ActivityLifecycleCallbacks {

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

	override fun onActivityStarted(activity: Activity) {}

	override fun onActivityResumed(activity: Activity) {
		// Clean up the activity name (e.g. MainActivity -> Main)
		val screenName = activity.javaClass.simpleName.replace("Activity", "")
		analyticsHelper.logScreenView(screenName, activity.javaClass.name)
	}

	override fun onActivityPaused(activity: Activity) {}

	override fun onActivityStopped(activity: Activity) {}

	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

	override fun onActivityDestroyed(activity: Activity) {}
}
