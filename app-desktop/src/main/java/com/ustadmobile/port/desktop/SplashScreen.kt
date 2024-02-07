package com.ustadmobile.port.desktop

import java.awt.Dimension
import java.awt.Frame
import java.awt.Graphics
import java.io.File
import javax.imageio.ImageIO

/**
 * Display a splash screen. We can't use the SplashScreen API because conveyor uses its own loader.
 */
class SplashScreen: Frame() {

    private val splashDir = File(ustadAppResourcesDir(), "splash")

    private val iconDir = File(ustadAppResourcesDir(), "icon")

    private val splashImage = ImageIO.read(File(splashDir, "splash.png"))

    private val iconImage = ImageIO.read(File(iconDir, "icon-512.png"))

    init {
        preferredSize = Dimension(splashImage.width, splashImage.height)
        isUndecorated = true
        pack()
        setLocationRelativeTo(null)
        setIconImage(iconImage)
        isVisible = true
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(splashImage, 0, 0, this)
    }

    fun close() {
        isVisible = false
    }

}