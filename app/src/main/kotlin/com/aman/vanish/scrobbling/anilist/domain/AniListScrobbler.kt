package com.aman.vanish.scrobbling.anilist.domain

import com.aman.vanish.core.db.MangaDatabase
import com.aman.vanish.core.parser.MangaRepository
import com.aman.vanish.scrobbling.anilist.data.AniListRepository
import com.aman.vanish.scrobbling.common.domain.Scrobbler
import com.aman.vanish.scrobbling.common.domain.model.ScrobblerService
import com.aman.vanish.scrobbling.common.domain.model.ScrobblingStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AniListScrobbler @Inject constructor(
	private val repository: AniListRepository,
	db: MangaDatabase,
	mangaRepositoryFactory: MangaRepository.Factory,
) : Scrobbler(db, ScrobblerService.ANILIST, repository, mangaRepositoryFactory) {

	init {
		statuses[ScrobblingStatus.PLANNED] = "PLANNING"
		statuses[ScrobblingStatus.READING] = "CURRENT"
		statuses[ScrobblingStatus.RE_READING] = "REPEATING"
		statuses[ScrobblingStatus.COMPLETED] = "COMPLETED"
		statuses[ScrobblingStatus.ON_HOLD] = "PAUSED"
		statuses[ScrobblingStatus.DROPPED] = "DROPPED"
	}

	override suspend fun updateScrobblingInfo(
		mangaId: Long,
		rating: Float,
		status: ScrobblingStatus?,
		comment: String?,
	) {
		val entity = db.getScrobblingDao().find(scrobblerService.id, mangaId)
		requireNotNull(entity) { "Scrobbling info for manga $mangaId not found" }
		repository.updateRate(
			rateId = entity.id,
			mangaId = entity.mangaId,
			rating = rating,
			status = statuses[status],
			comment = comment,
		)
	}
}
