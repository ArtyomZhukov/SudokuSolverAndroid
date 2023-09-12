package com.zhukovartemvl.sudokusolver.tools

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.zhukovartemvl.sudokusolver.R

class PermissionsChecker {

    fun checkAllPermissions(context: Context): Boolean {
        return checkOverlayPermissions(context) && checkAccessibilityPermissions(context)
    }

    fun checkOverlayPermissions(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun checkAccessibilityPermissions(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val serviceName = context.getString(R.string.accessibility_service_id)

        val skyAutoMusicServiceInfo = accessibilityManager.installedAccessibilityServiceList.find { service ->
            service.resolveInfo.serviceInfo.name == serviceName
        }?.resolveInfo?.serviceInfo ?: return false

        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        return enabledServices.any { enabledService ->
            val enabledServiceInfo = enabledService.resolveInfo.serviceInfo

            val samePackageName = enabledServiceInfo.packageName == skyAutoMusicServiceInfo.packageName
            val sameClassName = enabledServiceInfo.name == skyAutoMusicServiceInfo.name
            val serviceBindPermission = skyAutoMusicServiceInfo.permission == Manifest.permission.BIND_ACCESSIBILITY_SERVICE

            samePackageName && sameClassName && serviceBindPermission
        }
    }
}
