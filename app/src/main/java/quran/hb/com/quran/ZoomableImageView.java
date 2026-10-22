package quran.hb.com.quran;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * ImageView avec zoom pinch-to-zoom (sans défilement).
 * - Pinch : zoom 1x..4x
 * - Double-tap : reset zoom à 1x
 */
public class ZoomableImageView extends ImageView {

    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 4.0f;

    private final Matrix matrix = new Matrix();
    private float currentScale = 1.0f;

    // Pinch zoom
    private ScaleGestureDetector scaleDetector;

    // Double tap
    private long lastTapTime = 0;
    private static final long DOUBLE_TAP_MS = 300;

    // Position du pivot de zoom
    private float pivotX = 0, pivotY = 0;

    // Translation pour maintenir le zoom centré
    private float transX = 0, transY = 0;

    // Drag (pan) après zoom
    private float lastDragX, lastDragY;
    private boolean isDragging = false;
    private static final float DRAG_THRESHOLD = 10f;

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetZoom();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);

        int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastDragX = ev.getX();
                lastDragY = ev.getY();
                isDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && currentScale > 1.01f) {
                    float dx = ev.getX() - lastDragX;
                    float dy = ev.getY() - lastDragY;
                    if (!isDragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                        isDragging = true;
                    }
                    if (isDragging) {
                        transX += dx;
                        transY += dy;
                        clampTranslation();
                        applyMatrix();
                        lastDragX = ev.getX();
                        lastDragY = ev.getY();
                        return true;
                    }
                }
                lastDragX = ev.getX();
                lastDragY = ev.getY();
                break;

            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    // Détection double-tap pour reset
                    long now = System.currentTimeMillis();
                    if (now - lastTapTime < DOUBLE_TAP_MS) {
                        resetZoom();
                        lastTapTime = 0;
                        return true;
                    }
                    lastTapTime = now;
                }
                isDragging = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isDragging = false;
                break;
        }

        // Si zoom > 1 et en train de dragger, on consomme l'événement
        if (isDragging && currentScale > 1.01f) return true;

        // Sinon on laisse passer pour que le parent gère (tap, etc.)
        return super.onTouchEvent(ev);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            pivotX = detector.getFocusX();
            pivotY = detector.getFocusY();
            isDragging = false;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            float newScale = currentScale * factor;
            newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
            float realFactor = newScale / currentScale;
            currentScale = newScale;

            // Ajuster la translation pour que le pivot reste fixe
            transX = pivotX - realFactor * (pivotX - transX);
            transY = pivotY - realFactor * (pivotY - transY);
            clampTranslation();
            applyMatrix();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (currentScale <= MIN_SCALE + 0.05f) {
                resetZoom();
            }
        }
    }

    /** Restreint la translation pour ne pas afficher de zone vide */
    private void clampTranslation() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float maxTransX = w * (currentScale - 1f);
        float maxTransY = h * (currentScale - 1f);

        transX = Math.max(-maxTransX, Math.min(0, transX));
        transY = Math.max(-maxTransY, Math.min(0, transY));
    }

    private void applyMatrix() {
        matrix.reset();
        matrix.postScale(currentScale, currentScale);
        matrix.postTranslate(transX, transY);
        setImageMatrix(matrix);
    }

    /** Remet le zoom à 1x, centré */
    public void resetZoom() {
        currentScale = 1.0f;
        transX = 0;
        transY = 0;
        matrix.reset();
        setImageMatrix(matrix);
        setScaleType(ScaleType.FIT_XY);
    }
}
