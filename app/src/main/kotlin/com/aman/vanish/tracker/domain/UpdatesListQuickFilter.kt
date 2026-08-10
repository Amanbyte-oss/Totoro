package com.aman.vanish.tracker.domain

import com.aman.vanish.core.prefs.AppSettings
import com.aman.vanish.favourites.domain.FavouritesRepository
import com.aman.vanish.list.domain.ListFilterOption
import com.aman.vanish.list.domain.MangaListQuickFilter
import javax.inject.Inject

class UpdatesListQuickFilter @Inject constructor(
	private val favouritesRepository: FavouritesRepository,
	settings: AppSettings,
) : MangaListQuickFilter(settings) {

	override suspend fun getAvailableFilterOptions(): List<ListFilterOption> =
		favouritesRepository.getMostUpdatedCategories(
			limit = 4,
		).map {
			ListFilterOption.Favorite(it)
		}
}
