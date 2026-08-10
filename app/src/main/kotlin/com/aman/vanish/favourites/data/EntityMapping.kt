package com.aman.vanish.favourites.data

import com.aman.vanish.core.db.entity.toManga
import com.aman.vanish.core.db.entity.toMangaTags
import com.aman.vanish.core.model.FavouriteCategory
import com.aman.vanish.list.domain.ListSortOrder
import java.time.Instant

fun FavouriteCategoryEntity.toFavouriteCategory(id: Long = categoryId.toLong()) = FavouriteCategory(
	id = id,
	title = title,
	sortKey = sortKey,
	order = ListSortOrder(order, ListSortOrder.NEWEST),
	createdAt = Instant.ofEpochMilli(createdAt),
	isTrackingEnabled = track,
	isVisibleInLibrary = isVisibleInLibrary,
)

fun FavouriteManga.toManga() = manga.toManga(tags.toMangaTags(), null)

fun Collection<FavouriteManga>.toMangaList() = map { it.toManga() }
