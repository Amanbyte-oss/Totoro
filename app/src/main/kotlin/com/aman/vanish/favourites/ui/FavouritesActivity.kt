package com.aman.vanish.favourites.ui

import android.os.Bundle
import com.aman.vanish.core.nav.AppRouter
import com.aman.vanish.core.ui.FragmentContainerActivity
import com.aman.vanish.favourites.ui.list.FavouritesListFragment

class FavouritesActivity : FragmentContainerActivity(FavouritesListFragment::class.java) {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val categoryTitle = intent.getStringExtra(AppRouter.KEY_TITLE)
		if (categoryTitle != null) {
			title = categoryTitle
		}
	}
}
