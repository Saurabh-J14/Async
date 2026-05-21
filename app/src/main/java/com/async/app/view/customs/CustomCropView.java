package com.async.app.view.customs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomCropView extends View {

    private Bitmap sourceBitmap;
    private final Matrix transformMatrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    // Touch handling states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final float[] startPoint = new float[2];
    private final float[] midPoint = new float[2];
    private float oldDist = 1f;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private float translationX = 0f;
    private float translationY = 0f;

    // Viewport configuration
    private float circleRadius = 250f; // Default radius
    private final Paint overlayPaint = new Paint();
    private final Paint clearPaint = new Paint();
    private final Paint borderPaint = new Paint();

    public CustomCropView(Context context) {
        super(context);
        init(context);
    }

    public CustomCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        overlayPaint.setColor(Color.parseColor("#99000000")); // Translucent black overlay
        overlayPaint.setStyle(Paint.Style.FILL);

        clearPaint.setAntiAlias(true);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5f);
        borderPaint.setAntiAlias(true);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setBitmap(Bitmap bitmap) {
        this.sourceBitmap = bitmap;
        if (bitmap != null) {
            // Center the image initially
            post(new Runnable() {
                @Override
                public void run() {
                    centerBitmap();
                }
            });
        }
    }

    private void centerBitmap() {
        if (sourceBitmap == null || getWidth() == 0 || getHeight() == 0) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bmpWidth = sourceBitmap.getWidth();
        float bmpHeight = sourceBitmap.getHeight();

        float scale = Math.max(viewWidth / bmpWidth, viewHeight / bmpHeight);
        
        // Match viewport size
        circleRadius = Math.min(viewWidth, viewHeight) * 0.35f;

        transformMatrix.reset();
        transformMatrix.postScale(scale, scale);
        transformMatrix.postTranslate((viewWidth - bmpWidth * scale) / 2f, (viewHeight - bmpHeight * scale) / 2f);
        
        // Cache initial transforms
        float[] values = new float[9];
        transformMatrix.getValues(values);
        scaleFactor = values[Matrix.MSCALE_X];
        translationX = values[Matrix.MTRANS_X];
        translationY = values[Matrix.MTRANS_Y];

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() == 0 || getHeight() == 0) return;

        // 1. Draw source image under transformation
        if (sourceBitmap != null) {
            canvas.drawBitmap(sourceBitmap, transformMatrix, null);
        }

        // 2. Draw Translucent Overlay with Circular Cutout
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        
        // Draw the full translucent dark background
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        
        // Punch a circular hole in the center
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        canvas.drawCircle(centerX, centerY, circleRadius, clearPaint);
        
        canvas.restoreToCount(layerId);

        // 3. Draw outline stroke of the circular mask
        canvas.drawCircle(centerX, centerY, circleRadius, borderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(transformMatrix);
                startPoint[0] = event.getX();
                startPoint[1] = event.getY();
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(transformMatrix);
                    midPoint(midPoint, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    transformMatrix.set(savedMatrix);
                    float dx = event.getX() - startPoint[0];
                    float dy = event.getY() - startPoint[1];
                    transformMatrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        transformMatrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        transformMatrix.postScale(scale, scale, midPoint[0], midPoint[1]);
                    }
                }
                break;
        }

        invalidate();
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(float[] point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point[0] = x / 2;
        point[1] = y / 2;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            transformMatrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }
    }

    /**
     * Clears the current bitmap and recycles it to free up memory.
     */
    public void clear() {
        if (sourceBitmap != null && !sourceBitmap.isRecycled()) {
            sourceBitmap.recycle();
        }
        sourceBitmap = null;
        invalidate();
    }

    /**
     * Extracts and crops the circular section centered in the View.
     */
    public Bitmap getCroppedBitmap() {
        if (sourceBitmap == null) return null;

        try {
            // 1. Create a bitmap that matches the bounding box of the circular crop viewport
            int diameter = (int) (circleRadius * 2);
            if (diameter <= 0) {
                diameter = 500;
            }
            Bitmap croppedOutput = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(croppedOutput);

            // 2. Translate the canvas so that the top-left of the viewport center circle aligns with (0,0)
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            
            Matrix cropMatrix = new Matrix(transformMatrix);
            // Shift matrix relative to the crop viewport
            cropMatrix.postTranslate(-centerX + circleRadius, -centerY + circleRadius);

            // 3. Draw source image into the output bitmap using adjusted matrix
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawBitmap(sourceBitmap, cropMatrix, paint);

            // 4. Transform output into a perfect circle mask
            Bitmap roundedOutput = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
            Canvas roundedCanvas = new Canvas(roundedOutput);
            
            Paint maskPaint = new Paint();
            maskPaint.setAntiAlias(true);
            
            // Draw the circular shape outline base
            roundedCanvas.drawCircle(circleRadius, circleRadius, circleRadius, maskPaint);
            
            // Composite the cropped rectangle image on top of circle using SRC_IN
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            roundedCanvas.drawBitmap(croppedOutput, 0, 0, maskPaint);

            // Recycle intermediate bitmap to free memory immediately
            croppedOutput.recycle();

            return roundedOutput;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}

