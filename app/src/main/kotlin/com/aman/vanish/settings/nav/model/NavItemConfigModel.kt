package com.aman.vanish.settings.nav.model

import androidx.annotation.StringRes
import com.aman.vanish.core.prefs.NavItem
import com.aman.vanish.list.ui.model.ListModel

data class NavItemConfigModel(
	val item: NavItem,
	@StringRes val disabledHintResId: Int,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is NavItemConfigModel && other.item == item
	}
}
