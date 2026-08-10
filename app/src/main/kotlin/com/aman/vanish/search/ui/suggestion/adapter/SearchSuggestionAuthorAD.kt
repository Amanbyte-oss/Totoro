package com.aman.vanish.search.ui.suggestion.adapter

import android.view.View
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.aman.vanish.R
import com.aman.vanish.databinding.ItemSearchSuggestionQueryHintBinding
import com.aman.vanish.search.domain.SearchKind
import com.aman.vanish.search.ui.suggestion.SearchSuggestionListener
import com.aman.vanish.search.ui.suggestion.model.SearchSuggestionItem

fun searchSuggestionAuthorAD(
	listener: SearchSuggestionListener,
) = adapterDelegateViewBinding<SearchSuggestionItem.Author, SearchSuggestionItem, ItemSearchSuggestionQueryHintBinding>(
	{ inflater, parent -> ItemSearchSuggestionQueryHintBinding.inflate(inflater, parent, false) },
) {

	val viewClickListener = View.OnClickListener { _ ->
		listener.onQueryClick(item.name, SearchKind.AUTHOR, true)
	}

	binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_user, 0, 0, 0)
	binding.root.setOnClickListener(viewClickListener)

	bind {
		binding.root.text = item.name
	}
}
