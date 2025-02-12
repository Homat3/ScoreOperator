package com.water.scoreoperator.utils

import android.Manifest.permission
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat

enum class PermissionsManager(val permissionName: String) {
    INTERNET(permission.INTERNET);



    companion object{
        var allIsPermitted = mutableStateOf(false)

        fun toNameArray() : Array<String>{
        return Array(PermissionsManager.entries.count()) { PermissionsManager.entries[it].permissionName }
        }

        fun updatePermitted( activity : Activity ){
            var res = true

            for (permission in toNameArray()){
                res = res && ( ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED )
            }

            allIsPermitted.value = res
        }
    }
}

