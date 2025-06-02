package com.dubd.permissionsscore.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dubd.permissionsscore.AppInfo
import com.dubd.permissionsscore.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel to store and manage app data across the application
 * This improves performance by fetching the app list only once
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    // StateFlow to observe the list of installed apps
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Cache for app icons to improve performance
    private val iconCache = LruCache<String, Bitmap>(100) // Cache size of 100 icons
    
    init {
        // Load apps when ViewModel is created
        loadInstalledApps()
    }
    
    /**
     * Load the list of installed apps
     */
    fun loadInstalledApps() {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>()
                val apps = getInstalledApps(context)
                _installedApps.value = apps
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get app icon with caching
     */
    fun getAppIcon(packageName: String): Bitmap? {
        // Check if icon is in cache
        val cachedIcon = iconCache.get(packageName)
        if (cachedIcon != null) {
            return cachedIcon
        }
        
        // If not in cache, load it
        return try {
            val pm = getApplication<Application>().packageManager
            val icon = pm.getApplicationIcon(packageName).toBitmap()
            // Store in cache
            iconCache.put(packageName, icon)
            icon
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get detailed information about a specific app
     */
    suspend fun getAppDetails(packageName: String): AppInfo {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val perms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
            val granted = perms.count { perm ->
                pm.checkPermission(perm, packageName) == PackageManager.PERMISSION_GRANTED
            }
            
            AppInfo(
                name = pkgInfo.applicationInfo?.loadLabel(pm).toString() ?: "",
                packageName = packageName,
                grantedPermissions = granted,
                totalPermissions = perms.size,
                permissions = perms
            )
        }
    }
    
    /**
     * Get apps with warnings (permissions that don't match their category)
     */
    suspend fun getAppsWithWarnings(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val categories = DataManager.loadCategories(context)
            
            installedApps.value.filter { app ->
                val categoryName = DataManager.getAppCategory(context, app.packageName)
                val category = categories.find { it.name == categoryName }
                val expectedPermissions = category?.permissions?.filterValues { it == true }?.keys ?: emptySet()
                
                val grantedSpecificPermissions = app.permissions.filter { perm ->
                    DataManager.getAllAndroidPermissions().contains(perm) &&
                            pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED
                }
                
                grantedSpecificPermissions.any { perm -> !expectedPermissions.contains(perm) }
            }
        }
    }
    
    /**
     * Check for third-party apps
     */
    suspend fun getThirdPartyAppsCount(): Int {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val currentAppPackageName = context.packageName
            
            installedApps.value.count { app ->
                val isNonSystem = try {
                    val appInfo = pm.getApplicationInfo(app.packageName, 0)
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
                } catch (e: Exception) {
                    false
                }
                
                val isNotCurrentApp = app.packageName != currentAppPackageName
                val isNotGoogleApp = !app.packageName.startsWith("com.google.")
                val appName = app.name
                val isAuthorizedApp = AUTHORIZED_APPS.any { 
                    item -> item.trim().lowercase() == appName.lowercase() 
                }
                
                val installer = try {
                    pm.getInstallerPackageName(app.packageName)
                } catch (e: Exception) {
                    null
                }
                
                isNonSystem && isNotCurrentApp && isNotGoogleApp && 
                        installer != "com.android.vending" && !isAuthorizedApp
            }
        }
    }
    
    /**
     * Get the list of third-party apps
     */
    suspend fun getThirdPartyApps(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val currentAppPackageName = context.packageName
            
            installedApps.value.filter { app ->
                val isNonSystem = try {
                    val appInfo = pm.getApplicationInfo(app.packageName, 0)
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
                } catch (e: Exception) {
                    false
                }
                
                val installer = try {
                    pm.getInstallerPackageName(app.packageName)
                } catch (e: Exception) {
                    null
                }
                
                val isAuthorizedApp = AUTHORIZED_APPS.any { 
                    item -> item.trim().lowercase() == app.name.lowercase() 
                }
                
                isNonSystem && app.packageName != currentAppPackageName && 
                        !app.packageName.startsWith("com.google.") && 
                        installer != "com.android.vending" && !isAuthorizedApp
            }
        }
    }
    
    /**
     * Get installed apps from the device
     */
    private fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { app ->
                val perms = try {
                    pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
                        .requestedPermissions?.toList() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                
                val granted = perms.count { perm ->
                    try {
                        pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED
                    } catch (e: Exception) {
                        false
                    }
                }
                
                AppInfo(
                    name = app.loadLabel(pm).toString(),
                    packageName = app.packageName,
                    grantedPermissions = granted,
                    totalPermissions = perms.size,
                    permissions = perms
                )
            }.sortedBy { item-> item.name.lowercase() }
    }
    
    companion object {
        private val AUTHORIZED_APPS = listOf(
            "Weather",
            "Card Package",
            "Notes",
            "Recorder",
            "com.oneplus.gamespace.black.overlay",
            "Community",
            "OnePlus Widget",
            "com.oneplus.filemanager.white.overlay",
            "com.oneplus.filemanager.black.overlay",
            "OnePlus Store",
            "Wireless Earphones",
            "com.oneplus.gamespace.white.overlay"
        )
    }
}
