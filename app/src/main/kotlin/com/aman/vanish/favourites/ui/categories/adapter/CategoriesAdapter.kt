package com.aman.vanish.favourites.ui.categories.adapter

import com.aman.vanish.core.ui.ReorderableListAdapter
import com.aman.vanish.favourites.ui.categories.FavouriteCategoriesListListener
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.ListStateHolderListener
import com.aman.vanish.list.ui.adapter.emptyStateListAD
import com.aman.vanish.list.ui.adapter.loadingStateAD
import com.aman.vanish.list.ui.model.ListModel

class CategoriesAdapter(
	onItemClickListener: FavouriteCategoriesListListener,
	listListener: ListStateHolderListener,
) : ReorderableListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.CATEGORY_LARGE, categoryAD(onItemClickListener))
		addDelegate(ListItemType.NAV_ITEM, allCategoriesAD(onItemClickListener))
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(listListener))
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
	}
}
