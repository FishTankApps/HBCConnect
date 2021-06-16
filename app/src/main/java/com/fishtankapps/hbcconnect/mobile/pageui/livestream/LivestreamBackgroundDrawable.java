package com.fishtankapps.hbcconnect.mobile.pageui.livestream;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fishtankapps.hbcconnect.mobile.storage.LivestreamData;

public class LivestreamBackgroundDrawable extends Drawable {

    private final LivestreamData livestreamData;
    private final static int IMAGE_BORDER = 30;

    public LivestreamBackgroundDrawable(LivestreamData livestreamData) {
        this.livestreamData = livestreamData;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(livestreamData.getBackgroundImage() != null) {
            Bitmap bitmap = livestreamData.getBackgroundImage();

            float scale = ((float) getBounds().height() - IMAGE_BORDER - IMAGE_BORDER) / bitmap.getHeight();

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(getBounds().width() - bitmap.getWidth() * scale - IMAGE_BORDER, IMAGE_BORDER);

            Paint paint = new Paint();

            Shader shaderA = new LinearGradient((getBounds().width() - bitmap.getWidth() * scale - IMAGE_BORDER),
                    (getBounds().height() / 2f), getBounds().width() - 10,
                    (getBounds().height() / 2f), Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

            Shader shaderB = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaderB.setLocalMatrix(matrix);
            paint.setShader(new ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN));

            canvas.drawRect(new Rect(0, 0, 10000, 1000), paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
