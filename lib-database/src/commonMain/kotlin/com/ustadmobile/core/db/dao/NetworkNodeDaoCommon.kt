package com.ustadmobile.core.db.dao

object NetworkNodeDaoCommon {
    const val findByBluetoothAddrSql = "SELECT * from NetworkNode WHERE bluetoothMacAddress = :bluetoothAddress"
}