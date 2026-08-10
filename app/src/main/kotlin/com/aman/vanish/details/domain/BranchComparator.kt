package com.aman.vanish.details.domain

import com.aman.vanish.core.util.LocaleStringComparator
import com.aman.vanish.details.ui.model.MangaBranch

class BranchComparator : Comparator<MangaBranch> {

	private val delegate = LocaleStringComparator()

	override fun compare(o1: MangaBranch, o2: MangaBranch): Int = delegate.compare(o1.name, o2.name)
}
