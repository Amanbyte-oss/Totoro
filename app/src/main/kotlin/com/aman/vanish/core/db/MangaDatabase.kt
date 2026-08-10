package com.aman.vanish.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.aman.vanish.bookmarks.data.BookmarkEntity
import com.aman.vanish.bookmarks.data.BookmarksDao
import com.aman.vanish.core.db.dao.ChaptersDao
import com.aman.vanish.core.db.dao.MangaDao
import com.aman.vanish.core.db.dao.MangaSourcesDao
import com.aman.vanish.core.db.dao.PreferencesDao
import com.aman.vanish.core.db.dao.TagsDao
import com.aman.vanish.core.db.dao.TrackLogsDao
import com.aman.vanish.core.db.entity.ChapterEntity
import com.aman.vanish.core.db.entity.MangaEntity
import com.aman.vanish.core.db.entity.MangaPrefsEntity
import com.aman.vanish.core.db.entity.MangaSourceEntity
import com.aman.vanish.core.db.entity.MangaTagsEntity
import com.aman.vanish.core.db.entity.TagEntity
import com.aman.vanish.ai.db.AiCacheEntryDao
import com.aman.vanish.ai.db.AiCacheEntryEntity
import com.aman.vanish.ai.db.AiPromptHistoryDao
import com.aman.vanish.ai.db.AiPromptHistoryEntity
import com.aman.vanish.core.db.migrations.Migration10To11
import com.aman.vanish.core.db.migrations.Migration11To12
import com.aman.vanish.core.db.migrations.Migration12To13
import com.aman.vanish.core.db.migrations.Migration13To14
import com.aman.vanish.core.db.migrations.Migration14To15
import com.aman.vanish.core.db.migrations.Migration15To16
import com.aman.vanish.core.db.migrations.Migration16To17
import com.aman.vanish.core.db.migrations.Migration17To18
import com.aman.vanish.core.db.migrations.Migration18To19
import com.aman.vanish.core.db.migrations.Migration19To20
import com.aman.vanish.core.db.migrations.Migration1To2
import com.aman.vanish.core.db.migrations.Migration20To21
import com.aman.vanish.core.db.migrations.Migration21To22
import com.aman.vanish.core.db.migrations.Migration22To23
import com.aman.vanish.core.db.migrations.Migration23To24
import com.aman.vanish.core.db.migrations.Migration24To23
import com.aman.vanish.core.db.migrations.Migration24To25
import com.aman.vanish.core.db.migrations.Migration25To26
import com.aman.vanish.core.db.migrations.Migration26To27
import com.aman.vanish.core.db.migrations.Migration27To28
import com.aman.vanish.core.db.migrations.Migration28To29
import com.aman.vanish.core.db.migrations.Migration2To3
import com.aman.vanish.core.db.migrations.Migration3To4
import com.aman.vanish.core.db.migrations.Migration4To5
import com.aman.vanish.core.db.migrations.Migration5To6
import com.aman.vanish.core.db.migrations.Migration6To7
import com.aman.vanish.core.db.migrations.Migration7To8
import com.aman.vanish.core.db.migrations.Migration8To9
import com.aman.vanish.core.db.migrations.Migration9To10
import com.aman.vanish.core.util.ext.processLifecycleScope
import com.aman.vanish.explore.data.SourcePresetEntity
import com.aman.vanish.explore.data.SourcePresetsDao
import com.aman.vanish.favourites.data.FavouriteCategoriesDao
import com.aman.vanish.favourites.data.FavouriteCategoryEntity
import com.aman.vanish.favourites.data.FavouriteEntity
import com.aman.vanish.favourites.data.FavouritesDao
import com.aman.vanish.history.data.HistoryDao
import com.aman.vanish.history.data.HistoryEntity
import com.aman.vanish.local.data.index.LocalMangaIndexDao
import com.aman.vanish.local.data.index.LocalMangaIndexEntity
import com.aman.vanish.scrobbling.common.data.ScrobblingDao
import com.aman.vanish.scrobbling.common.data.ScrobblingEntity
import com.aman.vanish.stats.data.StatsDao
import com.aman.vanish.stats.data.StatsEntity
import com.aman.vanish.suggestions.data.SuggestionDao
import com.aman.vanish.suggestions.data.SuggestionEntity
import com.aman.vanish.tracker.data.TrackEntity
import com.aman.vanish.tracker.data.TrackLogEntity
import com.aman.vanish.tracker.data.TracksDao

const val DATABASE_VERSION = 29

@Database(
	entities = [
		MangaEntity::class, TagEntity::class, HistoryEntity::class, MangaTagsEntity::class, ChapterEntity::class,
		FavouriteCategoryEntity::class, FavouriteEntity::class, MangaPrefsEntity::class, TrackEntity::class,
		TrackLogEntity::class, SuggestionEntity::class, BookmarkEntity::class, ScrobblingEntity::class,
		MangaSourceEntity::class, StatsEntity::class, LocalMangaIndexEntity::class,
		SourcePresetEntity::class,
		AiPromptHistoryEntity::class,
		AiCacheEntryEntity::class,
	],
	version = DATABASE_VERSION,
)
abstract class MangaDatabase : RoomDatabase() {

	abstract fun getHistoryDao(): HistoryDao

	abstract fun getTagsDao(): TagsDao

	abstract fun getMangaDao(): MangaDao

	abstract fun getFavouritesDao(): FavouritesDao

	abstract fun getPreferencesDao(): PreferencesDao

	abstract fun getFavouriteCategoriesDao(): FavouriteCategoriesDao

	abstract fun getTracksDao(): TracksDao

	abstract fun getTrackLogsDao(): TrackLogsDao

	abstract fun getSuggestionDao(): SuggestionDao

	abstract fun getBookmarksDao(): BookmarksDao

	abstract fun getScrobblingDao(): ScrobblingDao

	abstract fun getSourcesDao(): MangaSourcesDao

	abstract fun getStatsDao(): StatsDao

	abstract fun getLocalMangaIndexDao(): LocalMangaIndexDao

	abstract fun getChaptersDao(): ChaptersDao

	abstract fun getSourcePresetsDao(): SourcePresetsDao

	abstract fun getAiPromptHistoryDao(): AiPromptHistoryDao

	abstract fun getAiCacheEntryDao(): AiCacheEntryDao
}

fun getDatabaseMigrations(context: Context): Array<Migration> = arrayOf(
	Migration1To2(),
	Migration2To3(),
	Migration3To4(),
	Migration4To5(),
	Migration5To6(),
	Migration6To7(),
	Migration7To8(),
	Migration8To9(),
	Migration9To10(),
	Migration10To11(),
	Migration11To12(),
	Migration12To13(),
	Migration13To14(),
	Migration14To15(),
	Migration15To16(),
	Migration16To17(context),
	Migration17To18(),
	Migration18To19(),
	Migration19To20(),
	Migration20To21(),
	Migration21To22(),
	Migration22To23(),
	Migration23To24(),
	Migration24To23(),
	Migration24To25(),
	Migration25To26(),
	Migration26To27(),
	Migration27To28(),
	Migration28To29(),
)

fun MangaDatabase(context: Context): MangaDatabase = Room
	.databaseBuilder(context, MangaDatabase::class.java, "kotatsu-db")
	.addMigrations(*getDatabaseMigrations(context))
	.addCallback(DatabasePrePopulateCallback(context.resources))
	.build()

fun InvalidationTracker.removeObserverAsync(observer: InvalidationTracker.Observer) {
	val scope = processLifecycleScope
	if (scope.isActive) {
		processLifecycleScope.launch(Dispatchers.Default, CoroutineStart.ATOMIC) {
			removeObserver(observer)
		}
	}
}
