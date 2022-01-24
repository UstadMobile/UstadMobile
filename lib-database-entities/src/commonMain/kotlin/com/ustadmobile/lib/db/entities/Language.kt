package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Language.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID, tracker = LanguageReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "language_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Language(langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, Language_Type, languageActive, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy, langLct) 
         VALUES (NEW.langUid, NEW.name, NEW.iso_639_1_standard, NEW.iso_639_2_standard, NEW.iso_639_3_standard, NEW.Language_Type, NEW.languageActive, NEW.langLocalChangeSeqNum, NEW.langMasterChangeSeqNum, NEW.langLastChangedBy, NEW.langLct) 
         /*psql ON CONFLICT (langUid) DO UPDATE 
         SET name = EXCLUDED.name, iso_639_1_standard = EXCLUDED.iso_639_1_standard, iso_639_2_standard = EXCLUDED.iso_639_2_standard, iso_639_3_standard = EXCLUDED.iso_639_3_standard, Language_Type = EXCLUDED.Language_Type, languageActive = EXCLUDED.languageActive, langLocalChangeSeqNum = EXCLUDED.langLocalChangeSeqNum, langMasterChangeSeqNum = EXCLUDED.langMasterChangeSeqNum, langLastChangedBy = EXCLUDED.langLastChangedBy, langLct = EXCLUDED.langLct
         */"""
     ]
 )
))
class Language() {

    @PrimaryKey(autoGenerate = true)
    var langUid: Long = 0

    var name: String? = null

    // 2 letter code
    var iso_639_1_standard: String? = null

    // 3 letter code
    var iso_639_2_standard: String? = null

    // 3 letter code
    var iso_639_3_standard: String? = null

    //Language Type - we are only normally interested in "L"
    var Language_Type: String? = null

    var languageActive: Boolean = true

    @LocalChangeSeqNum
    var langLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var langMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var langLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var langLct: Long = 0

    override fun toString(): String {
        return name.toString()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val language = other as Language?

        if (langUid != language!!.langUid) return false
        if (if (name != null) name != language.name else language.name != null) return false
        if (if (iso_639_1_standard != null) iso_639_1_standard != language.iso_639_1_standard else language.iso_639_1_standard != null)
            return false
        if (if (iso_639_2_standard != null) iso_639_2_standard != language.iso_639_2_standard else language.iso_639_2_standard != null)
            return false
        return if (iso_639_3_standard != null) iso_639_3_standard == language.iso_639_3_standard else language.iso_639_3_standard == null
    }

    override fun hashCode(): Int {
        var result = (langUid xor langUid.ushr(32)).toInt()
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        result = 31 * result + if (iso_639_1_standard != null) iso_639_1_standard!!.hashCode() else 0
        result = 31 * result + if (iso_639_2_standard != null) iso_639_2_standard!!.hashCode() else 0
        result = 31 * result + if (iso_639_3_standard != null) iso_639_3_standard!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 13

        const val ENGLISH_LANG_UID = 10000L

        const val ARABIC_LANG_UID = 10001L

        const val BENGALI_LANG_UID = 10002L

        const val BURMESE_LANG_UID = 10003L

        const val KINYARWANDA_LANG_UID = 10004L

        const val NEPALI_LANG_UID = 10005L

        const val PASHTO_LANG_UID = 10006L

        const val PERSIAN_LANG_UID = 10007L

        const val RUSSIAN_LANG_UID = 10008L

        const val TAJIK_LANG_UID = 10009L

        val FIXED_LANGUAGES = listOf(
                Language().apply{
                    name = "English"
                    langUid = ENGLISH_LANG_UID
                    iso_639_1_standard = "en"
                    iso_639_2_standard = "eng"
                    iso_639_3_standard = "eng"
                },
                Language().apply{
                    name = "العربية"
                    langUid = ARABIC_LANG_UID
                    iso_639_1_standard = "ar"
                    iso_639_2_standard = "ara"
                    iso_639_3_standard = "ara"
                },
                Language().apply{
                    name = "বাংলা"
                    langUid = BENGALI_LANG_UID
                    iso_639_1_standard = "bn"
                    iso_639_2_standard = "ben"
                    iso_639_3_standard = "ben"
                },
                Language().apply{
                    name = "မြန်မာ"
                    langUid = BURMESE_LANG_UID
                    iso_639_1_standard = "my"
                    iso_639_2_standard = "bur"
                    iso_639_3_standard = "mya"
                },
                Language().apply{
                    name = "Ikinyarwanda"
                    langUid = KINYARWANDA_LANG_UID
                    iso_639_1_standard = "rw"
                    iso_639_2_standard = "kin"
                    iso_639_3_standard = "kin"
                },
                Language().apply{
                    name = "नेपाली"
                    langUid = NEPALI_LANG_UID
                    iso_639_1_standard = "ne"
                    iso_639_2_standard = "nep"
                    iso_639_3_standard = "nep"
                },
                Language().apply{
                    name = "پښتو"
                    langUid = PASHTO_LANG_UID
                    iso_639_1_standard = "ps"
                    iso_639_2_standard = "pus"
                    iso_639_3_standard = "pus"
                },
                Language().apply{
                    name = "فارسی"
                    langUid = PERSIAN_LANG_UID
                    iso_639_1_standard = "fa"
                    iso_639_2_standard = "per"
                    iso_639_3_standard = "fas"
                },
                Language().apply{
                    name = "русский"
                    langUid = RUSSIAN_LANG_UID
                    iso_639_1_standard = "ru"
                    iso_639_2_standard = "rus"
                    iso_639_3_standard = "rus"
                },
                Language().apply{
                    name = "Тоҷикӣ"
                    langUid = TAJIK_LANG_UID
                    iso_639_1_standard = "tg"
                    iso_639_2_standard = "tgk"
                    iso_639_3_standard = "tgk"
                }
        )

    }
}
