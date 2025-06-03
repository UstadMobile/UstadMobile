package com.ustadmobile.centralappconfigdb.datasource

/**
 * Expected implementations:
 *
 *  DataSource_SqlDelight - implements the datasource saving to/from SQLDelight
 *
 *  DataSource_Network - implements the datasource using KTOR HTTP and/or Kotlinx RPC.
 *    This would be the only datasource used on web version.
 *
 *  DataSource_RPCServerImpl - performs any required permission check, once satisfied, delegates to
 *   DataSource_SqlDelight.
 *
 *  DataSource_Repository - mediates between a local datasource (DataSource_SqlDelight) and a
 *  DataSource_Network. Fetches anything that has changed since last check (use stored dates),
 *  upserts into local database. Changes item locally, then enqueues a job to run when connectivity
 *  is available to submit the update to the network.
 *
 *  DataSource interfaces will have a credentials / auth parameter if authentication is required.
 *
 *  Return results may need a wrapper class.
 *
 * When what we need to fetch is different to what we are returning (e.g. Gradebook queries return
 * info based on aggregates/subqueries) _then_
 * getGradebookResults repository implementation calls network.getLineItemsForGradebookResults(...)
 * and returns local.getGradebookResults
 */
interface CentralAppConfigDbDataSource {

    val learningSpaceDataSource: LearningSpaceDataSource

    companion object {

        const val PATH = "centralappconfig"

    }

}