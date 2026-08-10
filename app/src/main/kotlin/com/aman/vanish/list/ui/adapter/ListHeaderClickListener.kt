package com.aman.vanish.list.ui.adapter

import android.view.View
import com.aman.vanish.list.ui.model.ListHeader

interface ListHeaderClickListener {

	fun onListHeaderClick(item: ListHeader, view: View)
}
