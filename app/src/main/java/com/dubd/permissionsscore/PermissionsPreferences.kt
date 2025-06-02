package com.dubd.permissionsscore

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PermissionsPreferences {
    private const val PREFS_NAME = "permissions_categories"
    private const val HIGH_PERMISSIONS_KEY = "high_permissions"
    private const val MODERATE_PERMISSIONS_KEY = "moderate_permissions"
    private const val LOW_PERMISSIONS_KEY = "low_permissions"
    private const val FIRST_RUN_KEY = "permissions_first_run"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun initializePermissionsCategories(context: Context) {
        val prefs = getPrefs(context)
        val isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true)

        if (isFirstRun) {
            // Set default values for each category
            val highPermissions = listOf(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION"
            )

            val moderatePermissions = listOf(
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_CALL_LOG"
            )

            val lowPermissions = listOf(
                "android.permission.BLUETOOTH",
                "android.permission.READ_CALENDAR"
            )

            savePermissionsList(context, HIGH_PERMISSIONS_KEY, highPermissions)
            savePermissionsList(context, MODERATE_PERMISSIONS_KEY, moderatePermissions)
            savePermissionsList(context, LOW_PERMISSIONS_KEY, lowPermissions)

            // Mark first run as complete
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply()
        }
    }

    fun getHighPermissions(context: Context): List<String> {
        return getPermissionsList(context, HIGH_PERMISSIONS_KEY)
    }

    fun getModeratePermissions(context: Context): List<String> {
        return getPermissionsList(context, MODERATE_PERMISSIONS_KEY)
    }

    fun getLowPermissions(context: Context): List<String> {
        return getPermissionsList(context, LOW_PERMISSIONS_KEY)
    }

    fun movePermission(context: Context, permission: String, fromCategory: String, toCategory: String) {
        // Get current lists
        val fromList = when (fromCategory) {
            "high" -> getHighPermissions(context).toMutableList()
            "moderate" -> getModeratePermissions(context).toMutableList()
            "low" -> getLowPermissions(context).toMutableList()
            else -> return
        }

        val toList = when (toCategory) {
            "high" -> getHighPermissions(context).toMutableList()
            "moderate" -> getModeratePermissions(context).toMutableList()
            "low" -> getLowPermissions(context).toMutableList()
            else -> return
        }

        // Remove from source list
        if (fromList.contains(permission)) {
            fromList.remove(permission)
        }

        // Add to destination list if not already there
        if (!toList.contains(permission)) {
            toList.add(permission)
        }

        // Save updated lists
        when (fromCategory) {
            "high" -> savePermissionsList(context, HIGH_PERMISSIONS_KEY, fromList)
            "moderate" -> savePermissionsList(context, MODERATE_PERMISSIONS_KEY, fromList)
            "low" -> savePermissionsList(context, LOW_PERMISSIONS_KEY, fromList)
        }

        when (toCategory) {
            "high" -> savePermissionsList(context, HIGH_PERMISSIONS_KEY, toList)
            "moderate" -> savePermissionsList(context, MODERATE_PERMISSIONS_KEY, toList)
            "low" -> savePermissionsList(context, LOW_PERMISSIONS_KEY, toList)
        }
    }

    private fun getPermissionsList(context: Context, key: String): List<String> {
        val prefs = getPrefs(context)
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePermissionsList(context: Context, key: String, permissions: List<String>) {
        val prefs = getPrefs(context)
        val json = Json.encodeToString(permissions)
        prefs.edit().putString(key, json).apply()
    }
}
