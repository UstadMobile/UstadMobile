package com.ustadmobile.core.domain.navigation

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.viewmodel.account.addaccountselectneworexisting.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.core.viewmodel.site.COMMON_TOP_LEVEL_NAV_ITEMS
import com.ustadmobile.lib.db.entities.Site

/**
 * Provides the default destination according to the visible destination flag in site
 * according to learning space
 *
 * The default destination is:
 * When on a personal account learning space or local then will exclude ClazzList screen from
 * filteredDestinations
 *
 * When on a multi user learning space then will show all visible screen
 *
 * This is a trivial use case; its sole reason to exist is to provide a sensible single point of
 * truth
 */
class GetDefaultDestinationUseCase(
    private val systemUrlConfig: SystemUrlConfig,
    private val learningSpace: LearningSpace,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    suspend operator fun invoke(): String? {
        val effectiveDb = repo ?: db
        val site = effectiveDb.siteDao().getSiteAsync()
        val visibilityFlag = site?.bottomNavVisibilityFlag ?: 0L

        val isPersonalOrLocal = learningSpace.url == systemUrlConfig.newPersonalAccountsLearningSpaceUrl ||
                learningSpace.isLocal

        val filteredDestinations = if (isPersonalOrLocal) {
            COMMON_TOP_LEVEL_NAV_ITEMS.filterNot { it.flag == Site.SHOW_COURSE }
        } else {
            COMMON_TOP_LEVEL_NAV_ITEMS
        }

        return filteredDestinations.firstOrNull { dest ->
            visibilityFlag.hasFlag(dest.flag)
        }?.destRoute
    }
}
