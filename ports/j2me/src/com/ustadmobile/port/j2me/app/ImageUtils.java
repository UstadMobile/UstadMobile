/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.app;

//import com.sun.lwuit.Image;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
//import com.sun.lwuit.Graphics;


/**
 *
 * @author varuna
 */
public class ImageUtils {
    
    /**
  * This method resizes an image by resampling its pixels
  * @param src The image to be resized
  * @return The resized image
  */

    
  public static Image imageResize(Image src, int screenWidth, int screenHeight)
    {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Image tmp = Image.createImage(screenWidth, srcHeight);
        Graphics g = tmp.getGraphics();
        int ratio = (srcWidth << 16) / screenWidth;
        int pos = ratio / 2;

        // Horizontal Resize
        for (int x = 0; x < screenWidth; x++)
        {
            g.setClip(x, 0, 1, srcHeight);
            g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
            pos += ratio;
        }

        Image resizedImage = Image.createImage(screenWidth, screenHeight);
        g = resizedImage.getGraphics();
        ratio = (srcHeight << 16) / screenHeight;
        pos = ratio / 2;

        //Vertical resize
        for (int y = 0; y < screenHeight; y++) {
            g.setClip(0, y, screenWidth, 1);
            g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
            pos += ratio;
        }

        return resizedImage;
    }
    
    protected static Image resizeImage(Image image, int resizedWidth, int resizedHeight) {

    int width = image.getWidth();
    int height = image.getHeight();

    int[] in = new int[width];
    int[] out = new int[resizedWidth * resizedHeight];

    int dy, dx;
    for (int y = 0; y < resizedHeight; y++) {

        dy = y * height / resizedHeight;
        image.getRGB(in, 0, width, 0, dy, width, 1);

        for (int x = 0; x < resizedWidth; x++) {
            dx = x * width / resizedWidth;
            out[(resizedWidth * y) + x] = in[dx];
        }

    }

    return Image.createRGBImage(out, resizedWidth, resizedHeight, true);

}
    
}
