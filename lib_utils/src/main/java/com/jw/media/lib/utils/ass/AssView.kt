package com.jw.media.lib.utils.ass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 *Created by Joyce.wang on 2024/9/27 16:35
 *@Description TODO
 */
class AssView : View, IAssView {
    var paint: Paint = Paint()
    var assImage: Array<AssImage>?

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.flags = Paint.ANTI_ALIAS_FLAG
        isFocusableInTouchMode = false
        background = null
        //        Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bm);
//        canvas.drawColor(0xffffb6c1);
//        canvas.toString();
        //paint.setShader(new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        //      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (assImage != null && assImage!!.size > 0) {
            for (image in assImage!!) {
                canvas.drawBitmap(image.bitmap, image.x, image.y, paint)
            }
        }
    }

    fun render(assImage: Array<AssImage>?) {
        if (this.assImage != null && this.assImage!!.size > 0) {
            for (ai in this.assImage!!) {
                AssBitmapPool.recycle(ai.bitmap)
            }
        }

        this.assImage = assImage
        invalidate()
    }
}
