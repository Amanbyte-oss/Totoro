package com.aman.vanish.main.ui

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.aman.vanish.R
import com.aman.vanish.core.nav.AppRouter
import com.aman.vanish.core.ui.dialog.buildAlertDialog

class MainMenuProvider(
	private val context: Context,
	private val router: AppRouter,
	private val viewModel: MainViewModel,
) : MenuProvider {

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.opt_main, menu)
	}

	override fun onPrepareMenu(menu: Menu) {
		menu.findItem(R.id.action_incognito)?.isChecked =
			viewModel.isIncognitoModeEnabled.value
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
		R.id.action_settings -> {
			router.openSettings()
			true
		}

		R.id.action_incognito -> {
			viewModel.setIncognitoMode(!menuItem.isChecked)
			true
		}

		R.id.action_app_update -> {
			router.openExternalBrowser("https://totorohaven-blush.vercel.app/index.html#download")
			true
		}

		else -> false
	}
}
