package com.aman.vanish.ai

import androidx.work.WorkManager
import com.aman.vanish.core.ui.FragmentContainerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiPickActivity : FragmentContainerActivity(AiPickFragment::class.java) {

    @Inject
    lateinit var workManager: WorkManager

    override fun onStart() {
        super.onStart()
        // Schedule daily AI cache cleanup (11.3)
        AICacheCleanupWorker.schedule(workManager)
    }
}
