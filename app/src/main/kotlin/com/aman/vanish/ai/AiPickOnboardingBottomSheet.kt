package com.aman.vanish.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.aman.vanish.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * First-time user experience (FTUE) bottom sheet for AI Pick.
 * Shown once on first launch of the AI Pick screen; persisted via SharedPreferences.
 */
class AiPickOnboardingBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AiPickOnboardingBottomSheet"
        private const val PREFS_NAME = "ai_pick_prefs"
        private const val KEY_SEEN = "hasSeenAiPickOnboarding"

        fun shouldShow(context: android.content.Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            return !prefs.getBoolean(KEY_SEEN, false)
        }

        fun markSeen(context: android.content.Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SEEN, true).apply()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_ai_pick_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_onboarding_got_it)?.setOnClickListener {
            markSeen(requireContext())
            dismissAllowingStateLoss()
        }
    }
}
