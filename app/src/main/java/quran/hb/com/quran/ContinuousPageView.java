package quran.hb.com.quran;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * Défilement continu style PDF pour le mode paysage.
 * Toutes les pages sont empilées verticalement avec une ligne séparatrice entre elles.
 * Le chargement des bitmaps est paresseux (seulement les pages proches de la vue).
 */
public class ContinuousPageView extends ScrollView {

    public interface PageChangeListener {
        void onPageChanged(int pagerIndex); // index dans le ViewPager (608 - numPage)
        void onTap(float fracX, float fracY);
    }

    // Épaisseur du séparateur en dp
    private static final int SEP_DP = 6;
    // Nombre de pages à charger avant/après la page visible
    private static final int LOAD_RADIUS = 1;

    private LinearLayout container;
    private ArrayList<String> imagePaths;
    private final SparseArray<ImageView> imageViews = new SparseArray<>();
    private final SparseArray<View>      hlViews    = new SparseArray<>();
    private final SparseArray<Boolean>   loaded     = new SparseArray<>();

    private PageChangeListener listener;
    private int currentIndex = -1; // index pager courant (0-based dans imagePaths)
    private final int sepPx;

    // Détection de tap (distinguer tap vs scroll)
    private float tapStartX, tapStartY;
    private long  tapDownTime;
    private final int tapSlop;        // déplacement max en px pour considérer un tap
    private static final long TAP_MAX_MS = 350; // durée max d'un tap

    // Surlignage tilawa
    private int hlIndex = -1, hlLine = 0, hlPoint = 0;

    public ContinuousPageView(Context context) {
        this(context, null);
    }

    public ContinuousPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContinuousPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setFillViewport(false);
        sepPx  = Math.round(SEP_DP * context.getResources().getDisplayMetrics().density);
        tapSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(0xFF1A1A1A); // fond sombre entre les pages
        addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /** Initialise la vue avec la liste des chemins d'images et l'index de départ. */
    public void setImagePaths(final ArrayList<String> paths, final int startIndex) {
        this.imagePaths = paths;
        container.removeAllViews();
        imageViews.clear();
        hlViews.clear();
        loaded.clear();
        hlIndex = -1;

        final int n = paths.size();
        for (int i = 0; i < n; i++) {
            // ── Conteneur par page (RelativeLayout pour surlignage) ──
            android.widget.RelativeLayout pageContainer = new android.widget.RelativeLayout(getContext());
            pageContainer.setBackgroundColor(0xFFFFF4CB);

            // Image de la page
            ImageView iv = new ImageView(getContext());
            android.widget.RelativeLayout.LayoutParams ivLp =
                    new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            iv.setLayoutParams(ivLp);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setAdjustViewBounds(true);
            pageContainer.addView(iv);
            imageViews.put(i, iv);

            // Vue de surlignage (tilawa)
            View hl = new View(getContext());
            android.widget.RelativeLayout.LayoutParams hlLp =
                    new android.widget.RelativeLayout.LayoutParams(0, 0);
            hl.setLayoutParams(hlLp);
            hl.setBackgroundColor(0x66FFC107);
            hl.setVisibility(GONE);
            pageContainer.addView(hl);
            hlViews.put(i, hl);

            // Ajouter le conteneur de page dans le LinearLayout principal
            LinearLayout.LayoutParams pageLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            container.addView(pageContainer, pageLp);

            // ── Séparateur (sauf après la dernière page) ──
            if (i < n - 1) {
                View sep = new View(getContext());
                sep.setBackgroundColor(0xFF2C2C2C);
                LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, sepPx);
                container.addView(sep, sepLp);
            }
        }

        // Attendre que la mise en page soit faite avant de scroller
        post(new Runnable() {
            @Override
            public void run() {
                scrollToIndex(startIndex, false);
                loadVisiblePages();
            }
        });
    }

    /** Scroller vers la page d'index pagerIndex (0-based dans imagePaths). */
    public void scrollToIndex(int index, boolean smooth) {
        if (imagePaths == null || index < 0 || index >= imagePaths.size()) return;
        View pageContainer = container.getChildAt(index * 2); // *2 car séparateurs
        if (pageContainer == null) return;
        if (smooth) smoothScrollTo(0, pageContainer.getTop());
        else scrollTo(0, pageContainer.getTop());
    }

    public void setPageChangeListener(PageChangeListener l) {
        this.listener = l;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scroll & chargement paresseux
    // ──────────────────────────────────────────────────────────────────────

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        loadVisiblePages();
        applyHighlight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > 0) {
            loadVisiblePages();
            applyHighlight();
        }
    }

    private void loadVisiblePages() {
        if (imagePaths == null || imagePaths.isEmpty()) return;
        int scrollY = getScrollY();
        int height  = getHeight();
        if (height <= 0) return;

        // Page au centre de la vue
        int centerY   = scrollY + height / 2;
        int visible   = indexAtY(centerY);

        // Notifier changement de page
        if (visible != currentIndex) {
            currentIndex = visible;
            if (listener != null) listener.onPageChanged(visible);
        }

        // Charger les pages dans le rayon
        int from = Math.max(0, visible - LOAD_RADIUS);
        int to   = Math.min(imagePaths.size() - 1, visible + LOAD_RADIUS);
        for (int i = from; i <= to; i++) {
            if (!Boolean.TRUE.equals(loaded.get(i))) {
                loadPage(i);
            }
        }

        // Décharger les pages trop éloignées
        for (int i = 0; i < imagePaths.size(); i++) {
            if (Boolean.TRUE.equals(loaded.get(i)) && (i < from - 1 || i > to + 1)) {
                unloadPage(i);
            }
        }
    }

    /** Retourne l'index (0-based) de la page à la coordonnée Y (dans le ScrollView). */
    private int indexAtY(int y) {
        if (imagePaths == null) return 0;
        int n = imagePaths.size();
        for (int i = 0; i < n; i++) {
            View pageContainer = container.getChildAt(i * 2);
            if (pageContainer == null) continue;
            // Bas de la zone page+séparateur
            int bottom;
            if (i < n - 1) {
                View sep = container.getChildAt(i * 2 + 1);
                bottom = (sep != null) ? sep.getBottom() : pageContainer.getBottom();
            } else {
                bottom = pageContainer.getBottom();
            }
            if (y <= bottom || i == n - 1) return i;
        }
        return 0;
    }

    private void loadPage(final int index) {
        loaded.put(index, true);
        final ImageView iv = imageViews.get(index);
        if (iv == null || iv.getDrawable() != null) return;
        final String path = imagePaths.get(index);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = BitmapFactory.decodeFile(path);
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (bmp != null) iv.setImageBitmap(bmp);
                    }
                });
            }
        }).start();
    }

    private void unloadPage(int index) {
        loaded.put(index, false);
        ImageView iv = imageViews.get(index);
        if (iv != null) iv.setImageDrawable(null);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Surlignage tilawa
    // ──────────────────────────────────────────────────────────────────────

    public void setHighlight(int pagerIndex, int line, int point) {
        hlIndex = pagerIndex;
        hlLine  = line;
        hlPoint = point;
    }

    public void clearHighlight() {
        hlIndex = -1;
        for (int i = 0; i < hlViews.size(); i++) {
            View hl = hlViews.valueAt(i);
            if (hl != null) hl.setVisibility(GONE);
        }
    }

    public void applyHighlight() {
        for (int i = 0; i < hlViews.size(); i++) {
            int idx = hlViews.keyAt(i);
            View hl  = hlViews.valueAt(i);
            if (hl == null) continue;

            if (idx != hlIndex) {
                if (hl.getVisibility() != GONE) hl.setVisibility(GONE);
                continue;
            }
            ImageView iv = imageViews.get(idx);
            if (iv == null) continue;
            int w = iv.getWidth(), h = iv.getHeight();
            if (w <= 0 || h <= 0) continue;

            int lnClamped  = Math.max(1, Math.min(15, hlLine));
            int col        = Math.max(0, Math.min(3, 4 - hlPoint));
            int cw = w / 4, ch = h / 15;
            int left = iv.getLeft() + col * cw;
            int top  = iv.getTop()  + (lnClamped - 1) * ch;

            android.widget.RelativeLayout.LayoutParams lp =
                    new android.widget.RelativeLayout.LayoutParams(cw, ch);
            lp.leftMargin = left;
            lp.topMargin  = top;
            hl.setLayoutParams(lp);
            hl.setVisibility(VISIBLE);

            // Auto-scroll si le surlignage n'est pas visible
            int scrollY = getScrollY();
            int viewH   = getHeight();
            // top est relatif au conteneur de page ; ajouter l'offset du conteneur
            View pageContainer = container.getChildAt(idx * 2);
            int pageTop = (pageContainer != null) ? pageContainer.getTop() : 0;
            int absTop  = pageTop + top;
            if (absTop < scrollY || absTop + ch > scrollY + viewH) {
                smoothScrollTo(0, Math.max(0, absTop - viewH / 3));
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Tap (pour afficher la barre de menus) — seulement si PAS de scroll
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Mémoriser la position et le temps du contact initial
                tapStartX  = ev.getX();
                tapStartY  = ev.getY();
                tapDownTime = ev.getEventTime();
                break;

            case MotionEvent.ACTION_UP:
                // Vérifier si c'est un vrai tap : faible déplacement ET durée courte
                float dx = Math.abs(ev.getX() - tapStartX);
                float dy = Math.abs(ev.getY() - tapStartY);
                long  dt = ev.getEventTime() - tapDownTime;
                boolean isTap = (dx <= tapSlop && dy <= tapSlop && dt < TAP_MAX_MS);

                if (isTap && listener != null) {
                    float tapX = ev.getX();
                    float tapY = ev.getY() + getScrollY();
                    int w = getWidth();
                    int idx = indexAtY((int) tapY);
                    View pageContainer = container.getChildAt(idx * 2);
                    if (pageContainer != null) {
                        int pageTop = pageContainer.getTop();
                        ImageView iv = imageViews.get(idx);
                        if (iv != null) {
                            int ph = iv.getHeight();
                            if (ph > 0 && w > 0) {
                                float fx = Math.max(0f, Math.min(1f, tapX / w));
                                float fy = Math.max(0f, Math.min(1f, (tapY - pageTop) / ph));
                                listener.onTap(fx, fy);
                            }
                        }
                    }
                }
                // Sinon c'était un scroll : ne rien faire
                break;
        }
        return super.onTouchEvent(ev);
    }
}
