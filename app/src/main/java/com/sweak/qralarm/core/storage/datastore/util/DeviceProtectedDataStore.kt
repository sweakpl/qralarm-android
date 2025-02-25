package com.sweak.qralarm.core.storage.datastore.util

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File

object DeviceProtectedDataStore {
    fun create(
        context: Context,
        preferencesFileName: String,
        corruptionHandler: ReplaceFileCorruptionHandler<Preferences>
    ): DataStore<Preferences> {
        val deviceProtectedContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else context

        return PreferenceDataStoreFactory.create(
            produceFile = {
                File(
                    deviceProtectedContext.filesDir,
                    "datastore/$preferencesFileName.preferences_pb"
                )
            },
            corruptionHandler = corruptionHandler
        )
    }

    fun migrateToDeviceProtectedStorageIfRequired(
        context: Context,
        preferencesFileName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val preferencesFilesDirLocalPath = "datastore/$preferencesFileName.preferences_pb"

            val deviceProtectedPreferencesFile = File(
                context.createDeviceProtectedStorageContext().filesDir,
                preferencesFilesDirLocalPath
            )
            val credentialProtectedPreferencesFile = File(
                context.filesDir,
                preferencesFilesDirLocalPath
            )

            try {
                if (credentialProtectedPreferencesFile.exists() &&
                    !deviceProtectedPreferencesFile.exists()
                ) {
                    deviceProtectedPreferencesFile.parentFile?.mkdirs()
                    credentialProtectedPreferencesFile.copyTo(
                        target = deviceProtectedPreferencesFile,
                        overwrite = true
                    )
                    credentialProtectedPreferencesFile.delete()
                }
            } catch (exception: Exception) { /* no-op */ }
        }
    }
} 