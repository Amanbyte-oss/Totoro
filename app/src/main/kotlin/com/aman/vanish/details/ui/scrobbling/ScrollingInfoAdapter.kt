package com.aman.vanish.details.ui.scrobbling

import com.aman.vanish.core.nav.AppRouter
import com.aman.vanish.core.ui.BaseListAdapter
import com.aman.vanish.list.ui.model.ListModel

class ScrollingInfoAdapter(
	router: AppRouter,
) : BaseListAdapter<ListModel>() {

	init {
		delegatesManager.addDelegate(scrobblingInfoAD(router))
	}
}
