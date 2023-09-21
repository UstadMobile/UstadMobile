package com.ustadmobile.core.view

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint

interface AccountListView{

    companion object {

        const val VIEW_NAME = "AccountListView"

        /**
         * Where FILTER_BY_ENDPOINT is specified only accounts for the given endpoint will be
         * displayed. If the user clicks 'add account', the user will be taken directly to the
         * login screen for that server (e.g. they will never be taken to the server selection screen)
         */
        const val ARG_FILTER_BY_ENDPOINT = "filterByEndpoint"

        /**
         * The Active Account mode can be "header" or "inlist".
         *
         * Header shows the active account at the top with a profile and logout button
         * (e.g. useful for the normal account list page)
         *
         * Inlist shows the active account in the list of accounts itself. This is useful
         */
        const val ARG_ACTIVE_ACCOUNT_MODE = "activeAccountMode"

        const val ACTIVE_ACCOUNT_MODE_HEADER = "header"

        const val ACTIVE_ACCOUNT_MODE_INLIST = "inlist"

    }

}
