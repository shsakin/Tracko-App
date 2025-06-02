package com.dubd.permissionsscore

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object DataManager {
    private const val PREFS_NAME = "categories"
    private const val CATEGORIES_KEY = "categories"
    private const val FIRST_RUN_KEY = "isFirstRun"

    fun initializeData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true)

        if (isFirstRun) {
            val initialCategories = getInitialCategories()
            saveCategories(context, initialCategories)

            // Save package names for example apps
            initialCategories.forEach { category ->
                val exampleApps = getExampleAppsForCategory(category.name)
                exampleApps.forEach { app ->
                    saveAppCategory(context, app.packageName, category.name)
                }
            }

            // Mark first run as complete
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply()
        }
    }

    fun loadCategories(context: Context): List<Category> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(CATEGORIES_KEY, null) ?: return emptyList()
        return try {
            var cats: List<Category> = Json.decodeFromString(json)
            cats = cats.sortedBy { it -> it.name.lowercase() }
            return cats
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCategories(context: Context, categories: List<Category>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Json.encodeToString(categories)
        prefs.edit().putString(CATEGORIES_KEY, json).apply()
    }

    fun getAppCategory(context: Context, packageName: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(packageName, null)
    }

    fun saveAppCategory(context: Context, packageName: String, categoryName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(packageName, categoryName).apply()
    }

    fun clearAppCategory(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(packageName).apply()
    }

    fun getAllAndroidPermissions(): List<String> {
        return listOf(
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_PHONE_STATE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_CONTACTS",
            "android.permission.CAMERA",
            "android.permission.READ_CALL_LOG",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH",
            "android.permission.READ_CALENDAR"
        )
    }

    private fun getInitialCategories(): List<Category> {
        return listOf(
            Category("Weather", mapOf("android.permission.ACCESS_FINE_LOCATION" to true)),
            Category(
                "Video players and editors", mapOf(
                    "android.permission.CAMERA" to true,
                    "android.permission.RECORD_AUDIO" to true,
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true
                )
            ),
            Category("Travel", mapOf("android.permission.ACCESS_FINE_LOCATION" to true)),
            Category("Tools and Utilities", emptyMap()),
            Category("Sports", emptyMap()),
            Category(
                "Social media", mapOf(
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true
                )
            ),
            Category("Shopping apps", emptyMap()),
            Category(
                "Productivity", mapOf(
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true
                )
            ),
            Category("News and magazines", emptyMap()),
            Category(
                "Maps and navigation", mapOf(
                    "android.permission.ACCESS_FINE_LOCATION" to true
                )
            ),
            Category("Medical", emptyMap()),
            Category(
                "Libraries and demo(Ebooks)", mapOf(
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true
                )
            ),
            Category(
                "House and home", mapOf(
                    "android.permission.RECORD_AUDIO" to true
                )
            ),
            Category(
                "Health and fitness", mapOf(
                    "android.permission.BLUETOOTH" to true
                )
            ),
            Category("Games", emptyMap()),
            Category(
                "Food and drink", mapOf(
                    "android.permission.ACCESS_FINE_LOCATION" to true
                )
            ),
            Category(
                "Finance", mapOf(
                    "android.permission.READ_CONTACTS" to true
                )
            ),
            Category("Entertainment", emptyMap()),
            Category("Education", emptyMap()),
            Category(
                "Dating", mapOf(
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true,
                    "android.permission.ACCESS_FINE_LOCATION" to true
                )
            ),
            Category(
                "Communication", mapOf(
                    "android.permission.RECORD_AUDIO" to true,
                    "android.permission.CAMERA" to true,
                    "android.permission.WRITE_EXTERNAL_STORAGE" to true
                )
            ),
            Category("Comics", emptyMap()),
            Category("Business", emptyMap())
        )
    }

    private fun getExampleAppsForCategory(categoryName: String): List<AppInfo> {
        return when (categoryName) {
            "Weather" -> listOf(
                AppInfo("Weather Forecast", "com.weather.forecast", 0, 0),
                AppInfo("My Earthquake Alerts", "com.jrustonapps.myeqalerts", 0, 0)
            )

            "Video players and editors" -> listOf(
                AppInfo("CapCut", "com.lemon.lvoverseas", 0, 0),
                AppInfo("Filmora", "com.wondershare.filmorago", 0, 0)
            )

            "Travel" -> listOf(
                AppInfo("GoZayaan", "com.gozayaan", 0, 0),
                AppInfo("Grab", "com.grabtaxi.passenger", 0, 0),
                AppInfo("Booking.com", "com.booking", 0, 0),
                AppInfo("Airbnb", "com.airbnb.android", 0, 0)
            )

            "Tools and Utilities" -> listOf(
                AppInfo("RAR", "com.rarlab.rar", 0, 0),
                AppInfo("Secure VPN", "com.secure.vpn", 0, 0),
                AppInfo("AppLock", "com.domobile.applock", 0, 0),
                AppInfo("SHAREit", "com.lenovo.anyshare.gps", 0, 0)
            )

            "Sports" -> listOf(
                AppInfo("Cricbuzz", "com.cricbuzz.android", 0, 0),
                AppInfo("Cricket Line Guru", "com.app.cricketapp", 0, 0)
            )

            "Social media" -> listOf(
                AppInfo("TikTok", "com.zhiliaoapp.musically", 0, 0),
                AppInfo("Instagram", "com.instagram.android", 0, 0),
                AppInfo("Facebook", "com.facebook.katana", 0, 0),
                AppInfo("Reddit", "com.reddit.frontpage", 0, 0)
            )

            "Shopping apps" -> listOf(
                AppInfo("Amazon", "com.amazon.mShop.android.shopping", 0, 0),
                AppInfo("Meesho", "com.meesho.supply", 0, 0),
                AppInfo("Daraz", "com.daraz.android", 0, 0),
                AppInfo("Ali Express", "com.alibaba.aliexpresshd", 0, 0),
                AppInfo("Flipkart", "com.flipkart.android", 0, 0)
            )

            "Productivity" -> listOf(
                AppInfo("CamScanner", "com.intsig.camscanner", 0, 0),
                AppInfo("Chatbot", "ai.chat.gpt.app", 0, 0),
                AppInfo("Microsoft", "com.microsoft.office.officehubrow", 0, 0)
            )

            "News and magazines" -> listOf(
                AppInfo("BBC", "bbc.mobile.news.ww", 0, 0),
                AppInfo("CNN", "com.cnn.mobile.android.phone", 0, 0),
                AppInfo("The New York Times", "com.nytimes.android", 0, 0)
            )

            "Maps and navigation" -> listOf(
                AppInfo("Pathau", "com.pathao.user", 0, 0),
                AppInfo("Uber", "com.ubercab", 0, 0),
                AppInfo("JoyRide", "ph.joyride.rider", 0, 0)
            )

            "Medical" -> listOf(
                AppInfo("Shukhee", "com.shukhee.app", 0, 0),
                AppInfo("MedEx", "com.medex", 0, 0),
                AppInfo("DocTime", "com.doctime", 0, 0)
            )

            "Libraries and demo(Ebooks)" -> listOf(
                AppInfo("Holy Quran", "com.alquran", 0, 0),
                AppInfo("Holy Bible", "com.sirma.mobile.bible.android", 0, 0)
            )

            "House and home" -> listOf(
                AppInfo("Home Security Camera", "com.warden.cam", 0, 0),
                AppInfo("HelloTask", "com.hellotask", 0, 0)
            )

            "Health and fitness" -> listOf(
                AppInfo("MyFitnessPal", "com.myfitnesspal.android", 0, 0),
                AppInfo("Calorie Counter", "com.fatsecret.android", 0, 0),
                AppInfo("Step Tracker", "com.step.tracker", 0, 0)
            )

            "Games" -> listOf(
                AppInfo("Mobile Legends", "com.mobile.legends", 0, 0),
                AppInfo("Candy Crush", "com.king.candycrushsaga", 0, 0),
                AppInfo("Angry Birds", "com.rovio.angrybirds", 0, 0)
            )

            "Food and drink" -> listOf(
                AppInfo("Foodpanda", "com.global.foodpanda.android", 0, 0),
                AppInfo("Uber Eats", "com.ubercab.eats", 0, 0),
                AppInfo("Zomato", "com.application.zomato", 0, 0)
            )

            "Finance" -> listOf(
                AppInfo("Money Manager", "com.realbyteapps.moneymanagerfree", 0, 0),
                AppInfo("Monefy", "com.monefy.app.lite", 0, 0),
                AppInfo("bKash", "com.bkash.app", 0, 0),
                AppInfo("Nagad", "com.nagad.app", 0, 0),
                AppInfo("Rocket", "com.dutchbanglabank.rocket", 0, 0)
            )

            "Entertainment" -> listOf(
                AppInfo("Sony LIV", "com.sonyliv", 0, 0),
                AppInfo("Netflix", "com.netflix.mediaclient", 0, 0),
                AppInfo("Toffee", "com.toffee", 0, 0),
                AppInfo("Bongo", "com.bongo.bongobd", 0, 0),
                AppInfo("Amazon Prime", "com.amazon.avod.thirdpartyclient", 0, 0)
            )

            "Education" -> listOf(
                AppInfo("Photomath", "com.microblink.photomath", 0, 0),
                AppInfo("10 Minutes School", "com.tenminuteschool", 0, 0),
                AppInfo("Duolingo", "com.duolingo", 0, 0),
                AppInfo("Khan Academy", "org.khanacademy.android", 0, 0)
            )

            "Dating" -> listOf(
                AppInfo("Tinder", "com.tinder", 0, 0),
                AppInfo("Bumble", "com.bumble.app", 0, 0),
                AppInfo("Flirtify", "com.flirtify.app", 0, 0)
            )

            "Communication" -> listOf(
                AppInfo("Imo", "com.imo.android.imoim", 0, 0),
                AppInfo("Messenger", "com.facebook.orca", 0, 0),
                AppInfo("WeChat", "com.tencent.mm", 0, 0),
                AppInfo("Skype", "com.skype.raider", 0, 0),
                AppInfo("WhatsApp Business", "com.whatsapp.w4b", 0, 0),
                AppInfo("Viber", "com.viber.voip", 0, 0)
            )

            "Comics" -> listOf(
                AppInfo("MangaHub", "com.mangahub", 0, 0),
                AppInfo("WebToon", "com.naver.linewebtoon", 0, 0),
                AppInfo("Tapas", "com.tapastic", 0, 0),
                AppInfo("Wattpad", "com.wattpad", 0, 0)
            )

            "Business" -> listOf(
                AppInfo("Freelancer.com", "com.freelancer.android", 0, 0),
                AppInfo("UpWork", "com.upwork.android", 0, 0),
                AppInfo("Zoom Workplace", "us.zoom.videomeetings", 0, 0),
                AppInfo("Meta Business Suite", "com.facebook.pages.app", 0, 0)
            )

            else -> emptyList()
        }
    }
}

@Serializable
data class Category(
    val name: String,
    val permissions: Map<String, Boolean>
)

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    val grantedPermissions: Int,
    val totalPermissions: Int,
    val permissions: List<String> = emptyList()
)

//fun getInstalledApps(context: Context): List<AppInfo> {
//    val pm = context.packageManager
//    val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
//
//    return apps.filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
//        .map { app ->
//            val perms = pm.getPackageInfo(app.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
//                .requestedPermissions?.toList() ?: emptyList()
//            val granted = perms.count { perm ->
//                pm.checkPermission(perm, app.packageName) == android.content.pm.PackageManager.PERMISSION_GRANTED
//            }
//
//            AppInfo(
//                name = app.loadLabel(pm).toString(),
//                packageName = app.packageName,
//                grantedPermissions = granted,
//                totalPermissions = perms.size,
//                permissions = perms
//            )
//        }
//}
//
//fun getAppDetails(context: Context, packageName: String): AppInfo {
//    val pm = context.packageManager
//    val pkgInfo = pm.getPackageInfo(packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
//    val perms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
//    val granted = perms.count { perm ->
//        pm.checkPermission(perm, packageName) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    return AppInfo(
//        name = pkgInfo.applicationInfo?.loadLabel(pm).toString() ?: "",
//        packageName = packageName,
//        grantedPermissions = granted,
//        totalPermissions = perms.size,
//        permissions = perms
//    )
//}