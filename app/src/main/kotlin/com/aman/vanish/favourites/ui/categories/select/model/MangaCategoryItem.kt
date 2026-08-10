package com.aman.vanish.favourites.ui.categories.select.model

import com.google.android.material.checkbox.MaterialCheckBox.CheckedState
import com.aman.vanish.core.model.FavouriteCategory
import com.aman.vanish.list.ui.ListModelDiffCallback
import com.aman.vanish.list.ui.model.ListModel

data class MangaCategoryItem(
	val category: FavouriteCategory,
	@CheckedState val checkedState: Int,
	val isTrackerEnabled: Boolean,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is MangaCategoryItem && other.category.id == category.id
	}

	override fun getChangePayload(previousState: ListModel): Any? {
		return if (previousState is MangaCategoryItem && previousState.checkedState != checkedState) {
			ListModelDiffCallback.PAYLOAD_CHECKED_CHANGED
		} else {
			super.getChangePayload(previousState)
		}
	}
}
