package com.aman.vanish.list.ui.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.aman.vanish.core.util.ext.setTextAndVisible
import com.aman.vanish.databinding.ItemLoadingStateBinding
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.LoadingState

fun loadingStateAD() = adapterDelegateViewBinding<LoadingState, ListModel, ItemLoadingStateBinding>(
	{ inflater, parent -> ItemLoadingStateBinding.inflate(inflater, parent, false) },
) {

	bind {
		binding.textViewMessage.setTextAndVisible(item.textResId)
	}
}
