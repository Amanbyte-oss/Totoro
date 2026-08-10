package com.aman.vanish.explore.ui.model

import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.MangaCompactListModel

data class RecommendationsItem(
	val manga: List<MangaCompactListModel>
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is RecommendationsItem
	}
}
