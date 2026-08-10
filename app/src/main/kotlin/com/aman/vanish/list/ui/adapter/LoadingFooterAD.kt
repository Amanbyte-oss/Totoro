package com.aman.vanish.list.ui.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.aman.vanish.R
import com.aman.vanish.list.ui.model.ListModel
import com.aman.vanish.list.ui.model.LoadingFooter

fun loadingFooterAD() = adapterDelegate<LoadingFooter, ListModel>(R.layout.item_loading_footer) {
}