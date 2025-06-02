package com.dubd.permissionsscore

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import com.dubd.permissionsscore.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

object UploadHelper {

    private const val PREFS_NAME = "upload_prefs"
    private const val LAST_UPLOAD_KEY = "last_upload_timestamp"
    private const val DEVICE_ID_KEY = "unique_device_id"

    fun maybeUploadPoints(
        context: Context,
        pointsData: Map<String, Any>,
        appViewModel: AppViewModel
    ) {
        MainScope().launch {
            var newPoints = pointsData.toMutableMap()
            val warnings = appViewModel.getAppsWithWarnings()
            val warningsCount = warnings.size
            newPoints["warnings"] = warningsCount
            newPoints["highSensitivity"] = PermissionsPreferences.getHighPermissions(context).joinToString(",")
            newPoints["moderateSensitivity"] = PermissionsPreferences.getModeratePermissions(context).joinToString(",")
            newPoints["lowSensitivity"] = PermissionsPreferences.getLowPermissions(context).joinToString(",")

            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastUpload = sharedPrefs.getLong(LAST_UPLOAD_KEY, 0L)
            val now = System.currentTimeMillis()
            val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L

            if (now - lastUpload < sevenDaysMillis) {
                Log.d("UploadHelper", "Less than 7 days since last upload. Skipping.")
            } else {

                val uniqueDeviceId = getOrCreateDeviceId(context, sharedPrefs)
                val deviceInfo = getDeviceInfo()

                val db = FirebaseFirestore.getInstance()
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val deviceDoc = db.collection("devices").document(uniqueDeviceId)

                val weeklyData = hashMapOf(
                    "points" to newPoints,
                    "timestamp" to now
                )

                deviceDoc.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val newDoc = hashMapOf(
                            "deviceInfo" to deviceInfo,
                            "weeklyData" to hashMapOf(dateKey to weeklyData)
                        )
                        deviceDoc.set(newDoc)
                    } else {
                        deviceDoc.update("weeklyData.$dateKey", weeklyData)
                    }

                    sharedPrefs.edit().putLong(LAST_UPLOAD_KEY, now).apply()
                    Log.d("UploadHelper", "Points uploaded successfully.")
                }.addOnFailureListener {
                    Log.e("UploadHelper", "Failed to upload points: ${it.message}")
                }
            }
        }
    }

    private fun getOrCreateDeviceId(
        context: Context,
        sharedPrefs: android.content.SharedPreferences
    ): String {
        val existingId = sharedPrefs.getString(DEVICE_ID_KEY, null)
        if (existingId != null) return existingId

        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val timestamp = System.currentTimeMillis().toString()
        val raw = "$androidId-$timestamp-${Build.MODEL}"
        val newId = hashString("SHA-256", raw).take(20) // Shortened unique hash

        sharedPrefs.edit().putString(DEVICE_ID_KEY, newId).apply()
        return newId
    }

    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "androidVersion" to Build.VERSION.RELEASE,
            "sdkInt" to Build.VERSION.SDK_INT.toString()
        )
    }

    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest.getInstance(type).digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
