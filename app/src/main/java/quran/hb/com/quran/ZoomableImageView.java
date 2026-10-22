package quran.hb.com.quran;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * ImageView avec zoom pinch-to-zoom.
 *
 * Comportement :
 *  - Par défaut : FIT_XY (page entière, aucune zone noire)
 *  - Pinch vers l'extérieur : zoom avant (agrandissement dans la page)
 *  - Double-tap : retour à la page entière (FIT_XY)
 *  - Pan (glisser) quand zoomé
 *
 * Zoom min = 1.0 (page entière), Zoom max = 4.0
 */
public class ZoomableImageView extends ImageView {

    // ── Limites de zoom ────────────────────────────────────────────────────
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 4.0f;

    // ── Matrice de transformation ──────────────────────────────────────────
    private final Matrix matrix = new Matrix();
    /** Matrice de base FIT_XY : calculée une fois que la vue est mesurée */
    private final Matrix baseMatrix = new Matrix();
    private boolean baseReady = false;

    // ── Échelle et translation courantes (relatives à baseMatrix) ──────────
    private float currentScale = 1.0f;
    private float transX = 0f;
    private float transY = 0f;

    // ── Détecteur de pinch ─────────────────────────────────────────────────
    private ScaleGestureDetector scaleDetector;
    private float pivotX = 0f, pivotY = 0f;

    // ── Pan ────────────────────────────────────────────────────────────────
    private float lastDragX, lastDragY;
    private boolean isDragging = false;
    private static final float DRAG_SLOP = 8f;

    // ── Double-tap ─────────────────────────────────────────────────────────
    private long lastTapTime = 0;
    private static final long DOUBLE_TAP_MS = 300;

    // ── Constructeurs ──────────────────────────────────────────────────────

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // On travaille toujours en mode MATRIX
        super.setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new PinchListener());
    }

    // ── Initialisation de la matrice de base ───────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            computeBaseMatrix(w, h);
        }
    }

    @Override
    public void setImageDrawable(android.graphics.drawable.Drawable drawable) {
        super.setImageDrawable(drawable);
        // Recalculer la matrice de base si la vue est déjà mesurée
        if (getWidth() > 0 && getHeight() > 0) {
            computeBaseMatrix(getWidth(), getHeight());
        }
    }

    /**
     * Calcule baseMatrix = transformation qui affiche l'image en FIT_XY
     * (l'image remplit exactement la vue, comme fitXY).
     */
    private void computeBaseMatrix(int viewW, int viewH) {
        Drawable d = getDrawable();
        if (d == null) {
            // Pas encore d'image : matrice identité
            baseMatrix.reset();
            baseReady = false;
            return;
        }
        int imgW = d.getIntrinsicWidth();
        int imgH = d.getIntrinsicHeight();
        if (imgW <= 0 || imgH <= 0) {
            baseMatrix.reset();
            baseReady = false;
            return;
        }

        // Matrice qui mappe le rectangle de l'image sur le rectangle de la vue
        RectF src  = new RectF(0, 0, imgW, imgH);
        RectF dst  = new RectF(0, 0, viewW, viewH);
        baseMatrix.reset();
        baseMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
        baseReady = true;

        // Réinitialiser le zoom au-dessus de la nouvelle base
        currentScale = 1.0f;
        transX = 0f;
        transY = 0f;
        applyMatrix();
    }

    // ── Application de la matrice ──────────────────────────────────────────

    /**
     * Applique : matrice finale = baseMatrix * scale * translation
     */
    private void applyMatrix() {
        matrix.set(baseMatrix);
        // Zoom centré sur (pivotX, pivotY) dans les coordonnées de la vue
        matrix.postScale(currentScale, currentScale, pivotX, pivotY);
        matrix.postTranslate(transX, transY);
        setImageMatrix(matrix);
    }

    // ── Clamp de la translation ────────────────────────────────────────────

    /**
     * Empêche de montrer des zones vides (noires) au-delà des bords de l'image.
     */
    private void clampTranslation() {
        int vw = getWidth();
        int vh = getHeight();
        if (vw <= 0 || vh <= 0) return;

        // Les bords de l'image dans les coordonnées de la vue, après zoom
        float scaledW = vw * currentScale;
        float scaledH = vh * currentScale;

        // Origine de l'image zoomée = pivot - scale * pivot + trans
        float imgLeft  = pivotX * (1f - currentScale) + transX;
        float imgTop   = pivotY * (1f - currentScale) + transY;
        float imgRight = imgLeft + scaledW;
        float imgBot   = imgTop  + scaledH;

        float dx = 0, dy = 0;
        if (scaledW >= vw) {
            if (imgLeft > 0)        dx = -imgLeft;
            else if (imgRight < vw) dx = vw - imgRight;
        } else {
            // Image plus petite que la vue (ne devrait pas arriver avec min=1)
            dx = (vw - scaledW) / 2f - imgLeft;
        }
        if (scaledH >= vh) {
            if (imgTop > 0)         dy = -imgTop;
            else if (imgBot < vh)   dy = vh - imgBot;
        } else {
            dy = (vh - scaledH) / 2f - imgTop;
        }
        transX += dx;
        transY += dy;
    }

    // ── Reset ──────────────────────────────────────────────────────────────

    /** Retour à la page entière (FIT_XY) */
    public void resetZoom() {
        currentScale = 1.0f;
        transX = 0f;
        transY = 0f;
        pivotX = 0f;
        pivotY = 0f;
        if (baseReady) {
            applyMatrix();
        } else if (getWidth() > 0 && getHeight() > 0) {
            computeBaseMatrix(getWidth(), getHeight());
        }
    }

    /** Retourne vrai si un zoom est actuellement actif (scale > 1) */
    public boolean isZoomed() {
        return currentScale > 1.05f;
    }

    // ── Touch ──────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!baseReady) return super.onTouchEvent(ev);

        scaleDetector.onTouchEvent(ev);

        switch (ev.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastDragX = ev.getX();
                lastDragY = ev.getY();
                isDragging = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // Deuxième doigt posé : on va zoomer, on annule le pan
                isDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && currentScale > 1.01f) {
                    float dx = ev.getX() - lastDragX;
                    float dy = ev.getY() - lastDragY;
                    if (!isDragging &&
                            (Math.abs(dx) > DRAG_SLOP || Math.abs(dy) > DRAG_SLOP)) {
                        isDragging = true;
                    }
                    if (isDragging) {
                        transX += dx;
                        transY += dy;
                        clampTranslation();
                        applyMatrix();
                        lastDragX = ev.getX();
                        lastDragY = ev.getY();
                        return true; // consommé : pas de flip de page
                    }
                }
                lastDragX = ev.getX();
                lastDragY = ev.getY();
                break;

            case MotionEvent.ACTION_UP:
                if (!isDragging && !scaleDetector.isInProgress()) {
                    long now = System.currentTimeMillis();
                    if (now - lastTapTime < DOUBLE_TAP_MS) {
                        // Double-tap → reset
                        resetZoom();
                        lastTapTime = 0;
                        return true;
                    }
                    lastTapTime = now;
                }
                isDragging = false;
                break;
        }

        // Si on est en train de panner, on consomme pour éviter le flip de page
        if (isDragging && currentScale > 1.01f) return true;

        return super.onTouchEvent(ev);
    }

    // ── Pinch listener ─────────────────────────────────────────────────────

    private class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // Pivot = centre du pinch en coordonnées de la vue
            pivotX = detector.getFocusX();
            pivotY = detector.getFocusY();
            isDragging = false;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor   = detector.getScaleFactor();
            float newScale = currentScale * factor;
            newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

            // Le ratio réel appliqué (peut être réduit par les limites)
            float realFactor = newScale / currentScale;
            currentScale = newScale;

            // Ajuster la translation pour que le point sous le pivot reste fixe
            // La formule : après scale(f) autour de pivot,
            //   transX' = transX - (f-1) * (pivot - imgLeft)
            // Comme on part de trans=0 au premier appel, simplement :
            transX += (1f - realFactor) * (pivotX);
            transY += (1f - realFactor) * (pivotY);

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
}
