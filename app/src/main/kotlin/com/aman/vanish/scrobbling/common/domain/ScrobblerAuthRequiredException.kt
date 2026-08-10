package com.aman.vanish.scrobbling.common.domain

import okio.IOException
import com.aman.vanish.scrobbling.common.domain.model.ScrobblerService

class ScrobblerAuthRequiredException(
	val scrobbler: ScrobblerService,
) : IOException()
