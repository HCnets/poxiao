package com.poxiao.app.campus

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.poxiao.app.data.CampusLocation

data class MapLaunchResult(
    val opened: Boolean,
    val appName: String = "",
)

object MapNavigator {
    private data class MapApp(
        val label: String,
        val packageName: String,
        val routeUri: (CampusLocation) -> String,
        val poiUri: (CampusLocation) -> String,
    )

    private val apps = listOf(
        MapApp(
            label = "\u9ad8\u5fb7\u5730\u56fe",
            packageName = "com.autonavi.minimap",
            routeUri = { point ->
                "androidamap://route?sourceApplication=poxiao" +
                    "&dlat=${point.latitude}" +
                    "&dlon=${point.longitude}" +
                    "&dname=${Uri.encode(point.name)}" +
                    "&dev=0&t=2"
            },
            poiUri = { point ->
                "androidamap://poi?sourceApplication=poxiao&keywords=${Uri.encode(point.name)}"
            },
        ),
        MapApp(
            label = "\u767e\u5ea6\u5730\u56fe",
            packageName = "com.baidu.BaiduMap",
            routeUri = { point ->
                "baidumap://map/direction?destination=latlng:${point.latitude},${point.longitude}|name:${Uri.encode(point.name)}&mode=walking"
            },
            poiUri = { point ->
                "baidumap://map/marker?location=0,0&title=${Uri.encode(point.name)}&content=${Uri.encode(point.name)}"
            },
        ),
        MapApp(
            label = "\u817e\u8baf\u5730\u56fe",
            packageName = "com.tencent.map",
            routeUri = { point ->
                "qqmap://map/routeplan?type=walk&tocoord=${point.latitude},${point.longitude}&to=${Uri.encode(point.name)}"
            },
            poiUri = { point ->
                "qqmap://map/search?keyword=${Uri.encode(point.name)}"
            },
        ),
    )

    fun availableMapApps(context: Context): List<String> {
        val packageManager = context.packageManager
        return apps.mapNotNull { app ->
            val probe = Intent(Intent.ACTION_VIEW, Uri.parse(app.poiUri(CampusLocation("", "test", 0.0, 0.0)))).apply {
                `package` = app.packageName
            }
            if (canHandle(packageManager, probe)) app.label else null
        }
    }

    fun openCampusPoint(context: Context, point: CampusLocation): MapLaunchResult {
        val packageManager = context.packageManager
        val hasCoordinates = point.latitude != 0.0 || point.longitude != 0.0

        apps.forEach { app ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (hasCoordinates) app.routeUri(point) else app.poiUri(point))).apply {
                `package` = app.packageName
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (canHandle(packageManager, intent)) {
                return runCatching {
                    context.startActivity(intent)
                    MapLaunchResult(true, app.label)
                }.getOrElse { MapLaunchResult(false) }
            }
        }

        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=${Uri.encode(point.name)}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (canHandle(packageManager, fallbackIntent)) {
            return runCatching {
                context.startActivity(fallbackIntent)
                MapLaunchResult(true, "\u7cfb\u7edf\u5730\u56fe")
            }.getOrElse { MapLaunchResult(false) }
        }
        return MapLaunchResult(false)
    }

    private fun canHandle(packageManager: PackageManager, intent: Intent): Boolean {
        return intent.resolveActivity(packageManager) != null
    }
}
