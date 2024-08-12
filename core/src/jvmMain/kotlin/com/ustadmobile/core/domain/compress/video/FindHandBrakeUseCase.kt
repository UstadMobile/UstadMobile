package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.util.ext.isLinuxOs
import com.ustadmobile.lib.util.SysPathUtil
import io.github.z4kn4fein.semver.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Find a suitable version of HandBrakeCLI. HandBrake 1.6.0+ is required to provide AV1 encoding
 * support.
 *
 * This supports the Flatpak version on Ubuntu which needs to be run via flatpak. The .deb package
 * for the latest Ubuntu LTS as of this writing is HandBrake 1.5.x, which does not support AV1.
 *
 *
 */
class FindHandBrakeUseCase(
    private val specifiedLocation: String? = null,
    private val workingDir: String = System.getProperty("user.dir"),
    private val osName: String = System.getProperty("os.name"),
) {

    data class HandBrakeResult(
        val version: Version,
        val command: List<String>,
    )

    private suspend fun getHandbrakeVersion(cmd: List<String>): HandBrakeResult? {
        return withContext(Dispatchers.IO) {
            val process = ProcessBuilder(cmd + "--version")
                .directory(File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
            val outputJob = async {
                process.inputStream.bufferedReader().use { it.readText() }
            }
            val stdErrJob = async {
                process.errorStream.bufferedReader().use { it.readText() }
            }
            val processResult = process.waitFor()

            val output = outputJob.await()
            stdErrJob.await()

            if(processResult != 0){
                return@withContext null
            }

            /*
             * Handbrake --version should output a line that starts with HandBrake and then the version
             * number.
             */
            output.lines().firstOrNull {
                it.startsWith("HandBrake 1")
            }?.trim()?.split(" ")?.lastOrNull()?.let {
                Version.parse(it, strict = false)
            }?.takeIf { it > MIN_VERSION_SUPPORTED }?.let {
                HandBrakeResult(it, cmd)
            }
        }
    }

    suspend operator fun invoke(): HandBrakeResult? {
        val commandPaths = SysPathUtil.findCommandInPath(
            commandName = "HandBrakeCLI",
            manuallySpecifiedLocation = specifiedLocation?.let { File(it) },
        )?.let { listOf(it.absolutePath) }
        val commandHandBrake = commandPaths?.let {
            getHandbrakeVersion(commandPaths)
        }?.takeIf { it.version > MIN_VERSION_SUPPORTED }

        return if(isLinuxOs(osName)) {
            //look for flatpak handbrake cli version
            val flatpakCommands = SysPathUtil.findCommandInPath("flatpak")?.let {
                listOf(it.absolutePath, "run", "fr.handbrake.HandBrakeCLI")
            }

            val flatpakHandBrake = flatpakCommands?.let { getHandbrakeVersion(it) }

            if(commandHandBrake == null ||
                (flatpakHandBrake != null && flatpakHandBrake.version > commandHandBrake.version)
            ) {
                flatpakHandBrake
            }else {
                commandHandBrake
            }
        }else {
            commandHandBrake
        }
    }

    companion object {

        val MIN_VERSION_SUPPORTED = Version(major = 1, minor = 6, patch = 0)

    }

}