package com.aman.vanish.explore.ui.adapter

import android.view.View
import com.aman.vanish.list.ui.adapter.ListHeaderClickListener
import com.aman.vanish.list.ui.adapter.ListStateHolderListener

interface ExploreListEventListener : ListStateHolderListener, View.OnClickListener, ListHeaderClickListener
