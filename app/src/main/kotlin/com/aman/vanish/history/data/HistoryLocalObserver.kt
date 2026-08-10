package com.aman.vanish.history.data

import dagger.Reusable
import com.aman.vanish.core.db.MangaDatabase
import com.aman.vanish.core.db.entity.toManga
import com.aman.vanish.core.db.entity.toMangaTags
import com.aman.vanish.history.domain.model.MangaWithHistory
import com.aman.vanish.list.domain.ListFilterOption
import com.aman.vanish.list.domain.ListSortOrder
import com.aman.vanish.local.data.index.LocalMangaIndex
import com.aman.vanish.local.domain.LocalObserveMapper
import org.koitharu.kotatsu.parsers.model.Manga
import javax.inject.Inject

@Reusable
class HistoryLocalObserver @Inject constructor(
	localMangaIndex: LocalMangaIndex,
	private val db: MangaDatabase,
) : LocalObserveMapper<HistoryWithManga, MangaWithHistory>(localMangaIndex) {

	fun observeAll(
		order: ListSortOrder,
		filterOptions: Set<ListFilterOption>,
		limit: Int
	) = db.getHistoryDao().observeAll(order, filterOptions, limit).mapToLocal()

	override fun toManga(e: HistoryWithManga) = e.manga.toManga(e.tags.toMangaTags(), null)

	override fun toResult(e: HistoryWithManga, manga: Manga) = MangaWithHistory(
		manga = manga,
		history = e.history.toMangaHistory(),
	)
}
