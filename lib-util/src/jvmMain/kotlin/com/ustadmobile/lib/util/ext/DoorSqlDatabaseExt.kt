package com.ustadmobile.lib.util.ext

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.jdbc.ext.useResults
import com.ustadmobile.door.jdbc.ext.mapRows
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.util.escapeHTML
import java.io.File
import java.io.IOException
import java.util.*

actual fun DoorSqlDatabase.fixTincan() {

    val listToFix = mutableListOf<ContainerFilesWithContentEntry>()

    connection.prepareStatement("""
         SELECT containerUid, cefUid AS containerEntryFileUid, 
                cefPath AS containerEntryFilePath, 
                title AS contentEntryTitle, description AS contentEntryDesc, 
                entryId AS contentEntryId
           FROM ContainerEntryFile
                JOIN ContainerEntry
                ON ContainerEntry.cecefUid = ContainerEntryFile.cefUid
                        
                JOIN Container
                ON Container.containerUid = ContainerEntry.ceContainerUid
                        
                JOIN ContentEntry
                ON ContentEntry.contentEntryUid = Container.containerContentEntryUid
                AND cePath = 'tincan.xml'
                        
          WHERE title SIMILAR TO '%(&|>|<)%' 
             OR description SIMILAR TO '%(&|>|<)%'  
             OR entryId SIMILAR TO '%(&|>|<)%'
    """).use { stmt ->
        stmt.executeQuery().useResults { result ->
            result.mapRows { set ->
                listToFix.add(ContainerFilesWithContentEntry().apply {
                    this.containerEntryFilePath = set.getString("containerEntryFilePath")
                    this.containerEntryFileUid = set.getLong("containerEntryFileUid")
                    this.containerUid = set.getLong("containerUid")
                    this.contentEntryTitle = set.getString("contentEntryTitle")
                    this.contentEntryDesc = set.getString("contentEntryDesc")
                    this.contentEntryId = set.getString("contentEntryId")
                })
            }
        }
    }


    listToFix.forEach {

        val tinCan = """
                <?xml version="1.0" encoding="UTF-8"?>
                <tincan xmlns="http://projecttincan.com/tincan.xsd">
                    <activities>
                        <activity id="${it.contentEntryId?.escapeHTML()}" type="http://adlnet.gov/expapi/activities/module">
                            <name>${it.contentEntryTitle?.escapeHTML()}</name>
                            <description lang="en-US">${it.contentEntryDesc?.escapeHTML()}</description>
                            <launch lang="en-us">index.html</launch>
                        </activity>
                    </activities>
                </tincan>
            """.trimIndent()

        val tincanFile = File.createTempFile("h5p-tincan", "xml")
        tincanFile.writeText(tinCan)

        val pathToCurrentFile = it.containerEntryFilePath ?: return@forEach
        val oldFile = File(pathToCurrentFile)
        val parentDir = oldFile.parentFile
        val newFile = File(parentDir, "tincanh5pfix")

        val md5Sum = tincanFile.gzipAndGetMd5(newFile)
        val md5Hex = md5Sum.toHexString()

        val finalDestFile = File(parentDir, md5Hex)
        if(!newFile.renameTo(finalDestFile))
            throw IOException("Could not rename $newFile to $finalDestFile")


        this.execSQL("""
           UPDATE ContainerEntryFile 
              SET cefPath = '${finalDestFile.absolutePath}',
                  ceCompressedSize = ${finalDestFile.length()},
                  ceTotalSize = ${tincanFile.length()},
                  cefMd5 = '${Base64.getEncoder().encodeToString(md5Sum)}'
            WHERE cefUid = ${it.containerEntryFileUid}   
        """)


        if(this.dbType() == DoorDbType.POSTGRES){
            execSQL("""
                 UPDATE Container 
               SET cntNumEntries = COALESCE(
                   (SELECT COUNT(*) 
                      FROM ContainerEntry 
                     WHERE ceContainerUid = Container.containerUid), 0),
                   fileSize = COALESCE(
                   (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize 
                      FROM ContainerEntry
                      JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid
                     WHERE ContainerEntry.ceContainerUid = Container.containerUid), 0),
                   cntLct = ${getSystemTimeInMillis()}  
                     
             WHERE containerUid = ${it.containerUid}
            """)
        }

        tincanFile.delete()
        oldFile.delete()

    }


}