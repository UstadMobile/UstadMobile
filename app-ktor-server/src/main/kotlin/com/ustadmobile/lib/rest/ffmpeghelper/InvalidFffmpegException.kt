package com.ustadmobile.lib.rest.ffmpeghelper

import java.io.File

class InvalidFffmpegException(val ffmpegFile: File?, val ffprobeFile: File?): Exception()
