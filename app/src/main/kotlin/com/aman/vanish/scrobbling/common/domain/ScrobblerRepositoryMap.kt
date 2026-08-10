package com.aman.vanish.scrobbling.common.domain

import com.aman.vanish.scrobbling.anilist.data.AniListRepository
import com.aman.vanish.scrobbling.common.data.ScrobblerRepository
import com.aman.vanish.scrobbling.common.domain.model.ScrobblerService
import com.aman.vanish.scrobbling.kitsu.data.KitsuRepository
import com.aman.vanish.scrobbling.mal.data.MALRepository
import com.aman.vanish.scrobbling.shikimori.data.ShikimoriRepository
import javax.inject.Inject
import javax.inject.Provider

class ScrobblerRepositoryMap @Inject constructor(
	private val shikimoriRepository: Provider<ShikimoriRepository>,
	private val aniListRepository: Provider<AniListRepository>,
	private val malRepository: Provider<MALRepository>,
	private val kitsuRepository: Provider<KitsuRepository>,
) {

	operator fun get(scrobblerService: ScrobblerService): ScrobblerRepository = when (scrobblerService) {
		ScrobblerService.SHIKIMORI -> shikimoriRepository
		ScrobblerService.ANILIST -> aniListRepository
		ScrobblerService.MAL -> malRepository
		ScrobblerService.KITSU -> kitsuRepository
	}.get()
}
