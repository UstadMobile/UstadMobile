package com.ustadmobile.port.desktop

import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingUtilities

/**
 * Display a splash screen. We can't use the SplashScreen API because conveyor uses its own loader.
 */
class SplashScreen {

    @Volatile
    private var frame: JFrame? = null

    init {
        val splashDir = File(ustadAppResourcesDir(), "splash")
        val imageFile = File(splashDir, "splash.png")
        val image = ImageIO.read(imageFile)

        frame = JFrame().apply {
            contentPane.layout = BorderLayout()
            isUndecorated = true
            preferredSize = Dimension(image.width, image.height)
            contentPane.add(
                JLabel(ImageIcon(image))
            )
            pack()
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    fun close() {
        SwingUtilities.invokeLater {
            frame?.isVisible = false
            frame = null
        }

    }

}