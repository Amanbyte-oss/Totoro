package com.aman.vanish.scrobbling.common.ui.config.adapter

import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.core.ui.list.OnListItemClickListener
import com.aman.vanish.list.ui.adapter.ListItemType
import com.aman.vanish.list.ui.adapter.emptyStateListAD
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.scrobbling.common.domain.model.ScrobblingInfo

class ScrobblingMangaAdapter(
	clickListener: OnListItemClickListener<ScrobblingInfo>,
) : BaseListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.HEADER, scrobblingHeaderAD())
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(null))
		addDelegate(ListItemType.MANGA_SCROBBLING, scrobblingMangaAD(clickListener))
	}
}
