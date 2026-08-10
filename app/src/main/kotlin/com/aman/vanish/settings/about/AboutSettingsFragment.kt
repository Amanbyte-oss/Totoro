package com.aman.vanish.settings.about

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import com.aman.vanish.BuildConfig
import com.aman.vanish.R
import com.aman.vanish.core.nav.router
import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.core.ui.BasePreferenceFragment

@AndroidEntryPoint
class AboutSettingsFragment : BasePreferenceFragment(R.string.about) {

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		addPreferencesFromResource(R.xml.pref_about)
		findPreference<Preference>(AppSettings.KEY_APP_VERSION)?.run {
			title = getString(R.string.app_version, BuildConfig.VERSION_NAME)
		}
	}

	override fun onPreferenceTreeClick(preference: Preference): Boolean {
		return when (preference.key) {
			AppSettings.KEY_APP_VERSION -> {
				openLink(R.string.url_download, preference.title)
				true
			}

			"about_official_website" -> {
				openLink(R.string.url_official_website, preference.title)
				true
			}

			"about_contact" -> {
				openLink(R.string.url_contact, preference.title)
				true
			}

			else -> super.onPreferenceTreeClick(preference)
		}
	}

	private fun openLink(
		@StringRes url: Int,
		title: CharSequence?
	): Boolean = if (router.openExternalBrowser(getString(url), title)) {
		true
	} else {
		Snackbar.make(listView, R.string.operation_not_supported, Snackbar.LENGTH_SHORT).show()
		false
	}
}
