package dev.mslalith.focuslauncher.data.respository

import dev.mslalith.focuslauncher.data.database.dao.AppsDao
import dev.mslalith.focuslauncher.data.database.dao.HiddenAppsDao
import dev.mslalith.focuslauncher.data.database.entities.HiddenAppRoom
import dev.mslalith.focuslauncher.data.database.entities.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HiddenAppsRepo @Inject constructor(
    private val appsDao: AppsDao,
    private val hiddenAppsDao: HiddenAppsDao
) {
    val onlyHiddenAppsFlow: Flow<List<App>>
        get() = hiddenAppsDao.getHiddenAppsFlow().map { hiddenApps ->
            hiddenApps.mapNotNull { appsDao.getAppBy(it.packageName) }
        }

    suspend fun addToHiddenApps(app: App) {
        hiddenAppsDao.hideApp(HiddenAppRoom(app.packageName))
    }

    suspend fun removeFromHiddenApps(packageName: String) {
        hiddenAppsDao.unHideApp(HiddenAppRoom(packageName))
    }

    suspend fun clearHiddenApps() = hiddenAppsDao.clearHiddenApps()

    private suspend fun isHidden(packageName: String) = hiddenAppsDao.getHiddenAppBy(packageName) != null
}
