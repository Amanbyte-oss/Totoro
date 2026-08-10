package com.aman.vanish.explore.ui.model

import com.aman.vanish.list.ui.model.ListModel

data class ExploreButtons(
	val isRandomLoading: Boolean,
	val activePresetName: String? = null,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is ExploreButtons
	}
}
