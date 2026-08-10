package com.aman.vanish.explore.ui.adapter

import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.explore.ui.model.MangaSourceItem
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.emptyHintAD
import com.aman.vanish.list.ui.adapter.listHeaderAD
import com.aman.vanish.list.ui.adapter.loadingStateAD
import com.aman.vanish.list.ui.model.ListModel
import org.koitharu.kotatsu.parsers.model.Manga

class ExploreAdapter(
	listener: ExploreListEventListener,
	clickListener: OnListItemClickListener<MangaSourceItem>,
	mangaClickListener: OnListItemClickListener<Manga>,
) : BaseListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.EXPLORE_BUTTONS, exploreButtonsAD(listener))
		addDelegate(
			ListItemType.EXPLORE_SUGGESTION,
			exploreRecommendationItemAD(mangaClickListener),
		)
		addDelegate(ListItemType.HEADER, listHeaderAD(listener))
		addDelegate(ListItemType.EXPLORE_SOURCE_LIST, exploreSourceListItemAD(clickListener))
		addDelegate(ListItemType.EXPLORE_SOURCE_GRID, exploreSourceGridItemAD(clickListener))
		addDelegate(ListItemType.HINT_EMPTY, emptyHintAD(listener))
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
	}
}
