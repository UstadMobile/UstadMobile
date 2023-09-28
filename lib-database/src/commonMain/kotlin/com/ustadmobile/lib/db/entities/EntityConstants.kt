package com.ustadmobile.lib.db.entities

const val TRIGGER_UPSERT_WHERE_NEWER = """
    REPLACE INTO %TABLE_AND_FIELD_NAMES%
                      SELECT %NEW_VALUES%
                       WHERE %NEW_ETAG_NOT_EQUAL_TO_EXISTING%
"""