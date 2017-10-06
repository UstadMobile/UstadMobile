package com.ustadmobile.port.android.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ImageLoader;
import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 8/4/17.
 */

/**
 * Wrapper for ImageLoadTarget
 */
public class ImageViewLoadTarget implements ImageLoader.ImageLoadTarget {

    private ImageView imageView;

    private UstadView ustadView;

    public ImageViewLoadTarget(UstadView ustadView, ImageView imageView) {
        this.imageView = imageView;
        this.ustadView = ustadView;
    }

    @Override
    public void setImageFromFile(String filePath) {
        final Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if(bitmap != null) {
            ustadView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }else {
            UstadMobileSystemImpl.l(UMLog.ERROR, 656, filePath);
        }
    }
}
