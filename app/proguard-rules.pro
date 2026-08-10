    -optimizationpasses 8
-dontobfuscate
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void checkExpressionValueIsNotNull(...);
	public static void checkNotNullExpressionValue(...);
	public static void checkReturnedValueIsNotNull(...);
	public static void checkFieldIsNotNull(...);
	public static void checkParameterIsNotNull(...);
	public static void checkNotNullParameter(...);
}

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn coil3.PlatformContext

-keep class com.aman.vanish.settings.NotificationSettingsLegacyFragment
-keep class com.aman.vanish.settings.about.changelog.ChangelogFragment

-keep class com.aman.vanish.core.exceptions.* { *; }
-keep class com.aman.vanish.core.prefs.ScreenshotsPolicy { *; }
-keep class com.aman.vanish.backups.ui.periodical.PeriodicalBackupSettingsFragment { *; }
-keep class org.jsoup.parser.Tag
-keep class org.jsoup.internal.StringUtil

-keep class org.acra.security.NoKeyStoreFactory { *; }
-keep class org.acra.config.DefaultRetryPolicy { *; }
-keep class org.acra.attachment.DefaultAttachmentProvider { *; }
-keep class org.acra.sender.JobSenderService

# AI Pick (Part 11) — keep all data classes used by Room, Gson, and Retrofit
-keep class com.aman.vanish.ai.** { *; }
-keep class com.aman.vanish.ai.models.** { *; }
-keep class com.aman.vanish.ai.db.** { *; }
# Keep Gemini API request/response models used with Gson
-keepclassmembers class com.aman.vanish.ai.** {
    <fields>;
}

