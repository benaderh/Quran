package quran.hb.com.quran;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * ScrollView d'une page en mode horizontal (paysage).
 * - continuer a faire defiler au-dela du bas  -> page suivante
 * - continuer a faire defiler au-dela du haut -> page precedente
 * - un simple clic est signale avec sa position relative dans la page (0..1)
 */
public class PageScrollView extends ScrollView {

    public interface EdgeListener {
        void onReachEnd();
        void onReachStart();
        void onTap(float fracX, float fracY);
    }

    private EdgeListener listener;
    private float startX, startY;
    private long downTime;
    private float bottomRefY = Float.NaN;
    private float topRefY = Float.NaN;
    private boolean flipped;
    private final int slop;
    private final float flipDistance;

    public PageScrollView(Context context) {
        this(context, null);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
        flipDistance = 48 * context.getResources().getDisplayMetrics().density;
        // Supprime l'effet de rebond/la ligne visible entre les pages
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setEdgeListener(EdgeListener l) {
        this.listener = l;
    }

    private boolean isAtTop() {
        return getScrollY() <= 0;
    }

    private boolean isAtBottom() {
        View c = getChildAt(0);
        if (c == null) return true;
        return c.getBottom() - (getHeight() + getScrollY()) <= 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                downTime = ev.getEventTime();
                flipped = false;
                bottomRefY = Float.NaN;
                topRefY = Float.NaN;
                break;

            case MotionEvent.ACTION_MOVE:
                if (flipped) return true;
                if (listener != null) {
                    float y = ev.getY();
                    // en bas de la page et on continue a tirer vers le haut
                    if (isAtBottom()) {
                        if (Float.isNaN(bottomRefY)) bottomRefY = y;
                        if (bottomRefY - y > flipDistance) {
                            flipped = true;
                            listener.onReachEnd();
                            return true;
                        }
                    } else {
                        bottomRefY = Float.NaN;
                    }
                    // en haut de la page et on continue a tirer vers le bas
                    if (isAtTop()) {
                        if (Float.isNaN(topRefY)) topRefY = y;
                        if (y - topRefY > flipDistance) {
                            flipped = true;
                            listener.onReachStart();
                            return true;
                        }
                    } else {
                        topRefY = Float.NaN;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (flipped) return true;
                if (listener != null
                        && Math.abs(ev.getX() - startX) < slop
                        && Math.abs(ev.getY() - startY) < slop
                        && ev.getEventTime() - downTime < 350) {
                    View c = getChildAt(0);
                    int w = getWidth();
                    int h = (c != null) ? c.getHeight() : 0;
                    if (w > 0 && h > 0) {
                        float fx = ev.getX() / w;
                        float fy = (ev.getY() + getScrollY()) / h;
                        fx = Math.max(0f, Math.min(1f, fx));
                        fy = Math.max(0f, Math.min(1f, fy));
                        listener.onTap(fx, fy);
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
}
