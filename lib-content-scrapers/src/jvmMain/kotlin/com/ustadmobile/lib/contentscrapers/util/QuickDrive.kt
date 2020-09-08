/*
package com.ustadmobile.lib.contentscrapers.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import java.io.*
import java.util.*


class QuickDrive {


    companion object {

        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private val APPLICATION_NAME = "Google Drive API Java Quickstart"
        private const val TOKENS_DIRECTORY_PATH = "tokens"

        private const val CREDENTIALS_FILE_PATH = "/com/ustadmobile/lib/contentscrapers/drive/credentials.json"

        val SCOPES: MutableList<String> = Collections.singletonList(DriveScopes.DRIVE_FILE)


        @Throws(IOException::class)
        private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? {
            // Load client secrets.
            val `in`: InputStream = QuickDrive::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                    ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

            // Build flow and trigger user authorization request.
            val flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build()
            val receiver = (LocalServerReceiver.Builder().setPort(8888).build())
            return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }

        @JvmStatic
        fun main(args: Array<String>) {

            val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            val service = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build()

            // Print the names and IDs for up to 10 files.
            val result: FileList = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute()
            val files: MutableList<com.google.api.services.drive.model.File>? = result.files
            if (files == null || files.isEmpty()) {
                println("No files found.")
            } else {
                println("Files:")
                for (file in files) {
                    System.out.printf("%s (%s)\n", file.name, file.id)
                }
            }

        }


    }

}


*/
