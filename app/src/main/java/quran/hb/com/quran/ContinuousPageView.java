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
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * Défilement continu style PDF pour le mode paysage.
 * Pages empilées verticalement avec une ligne séparatrice.
 * Chargement paresseux + anti-scintillement par token.
 *
 * Ordre : index 0 = première page Coran (haut), scroll BAS = page suivante.
 * Les imagePaths sont utilisés tels quels, l'appelant les inverse si besoin.
 */
public class ContinuousPageView extends ScrollView {

    // ── Interface ──────────────────────────────────────────────────────────
    public interface PageChangeListener {
        /** pagerIndex = index dans la liste imagePaths (0-based) */
        void onPageChanged(int pagerIndex);
        void onTap(float fracX, float fracY);
    }

    // ── Constantes ─────────────────────────────────────────────────────────
    private static final int SEP_DP          = 6;    // épaisseur séparateur
    private static final int LOAD_RADIUS     = 1;    // pages chargées autour de la visible
    private static final long TAP_MAX_MS     = 350;  // durée max tap (ms)
    private static final float QURAN_RATIO   = 1.41f;// ratio H/W par défaut (A4)

    // ── Vues ───────────────────────────────────────────────────────────────
    private LinearLayout container;
    private ArrayList<String> imagePaths;

    private final SparseArray<ImageView> imageViews   = new SparseArray<ImageView>();
    private final SparseArray<View>      hlViews      = new SparseArray<View>();
    /** Numéro de version par index : incrémenté à chaque unload pour annuler les threads */
    private final SparseArray<Integer>   loadToken    = new SparseArray<Integer>();
    /** true si le chargement est déclenché (thread lancé ou image présente) */
    private final SparseArray<Boolean>   loadTriggered = new SparseArray<Boolean>();

    // ── État ───────────────────────────────────────────────────────────────
    private PageChangeListener listener;
    private int  currentIndex  = -1;
    private final int sepPx;
    private int  pageHeightPx  = 0; // calculé depuis les vraies dimensions de l'image
    private int  pendingStart  = -1;// index à afficher dès que pageHeightPx est connu

    // ── Détection tap ──────────────────────────────────────────────────────
    private float tapStartX, tapStartY;
    private long  tapDownTime;
    private final int tapSlop;

    // ── Surlignage tilawa ──────────────────────────────────────────────────
    private int hlIndex = -1, hlLine = 0, hlPoint = 0;

    // ══════════════════════════════════════════════════════════════════════
    // Constructeurs
    // ══════════════════════════════════════════════════════════════════════

    public ContinuousPageView(Context context) { this(context, null); }
    public ContinuousPageView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public ContinuousPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOverScrollMode(OVER_SCROLL_NEVER);
        float density = context.getResources().getDisplayMetrics().density;
        sepPx    = Math.round(SEP_DP * density);
        tapSlop  = ViewConfiguration.get(context).getScaledTouchSlop();

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(0xFF1A1A1A); // fond sombre entre les pages
        addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Initialisation des pages
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Charge la liste des images et positionne la vue sur startIndex.
     * Les pages sont affichées dans l'ordre de la liste (index 0 en haut).
     */
    public void setImagePaths(final ArrayList<String> paths, final int startIndex) {
        this.imagePaths = paths;
        container.removeAllViews();
        imageViews.clear();
        hlViews.clear();
        loadToken.clear();
        loadTriggered.clear();
        hlIndex       = -1;
        pageHeightPx  = 0;
        currentIndex  = -1;
        pendingStart  = startIndex;

        final int n = paths.size();

        // Construction des vues avec une hauteur temporaire de 0
        // (sera mise à jour dès qu'on connaît les dimensions réelles)
        for (int i = 0; i < n; i++) {
            // Conteneur de la page
            RelativeLayout pageContainer = new RelativeLayout(getContext());
            pageContainer.setBackgroundColor(0xFFFFF4CB);

            // ImageView
            ImageView iv = new ImageView(getContext());
            RelativeLayout.LayoutParams ivLp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            iv.setLayoutParams(ivLp);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setAdjustViewBounds(false);
            iv.setBackgroundColor(0xFFFFF4CB);
            pageContainer.addView(iv);
            imageViews.put(i, iv);
            loadToken.put(i, 0);
            loadTriggered.put(i, false);

            // Vue de surlignage tilawa
            View hl = new View(getContext());
            hl.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            hl.setBackgroundColor(0x66FFC107);
            hl.setVisibility(GONE);
            pageContainer.addView(hl);
            hlViews.put(i, hl);

            // Hauteur = 0 tant qu'on ne connaît pas les dimensions réelles
            LinearLayout.LayoutParams pageLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0);
            container.addView(pageContainer, pageLp);

            // Séparateur
            if (i < n - 1) {
                View sep = new View(getContext());
                sep.setBackgroundColor(0xFF2C2C2C);
                container.addView(sep, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, sepPx));
            }
        }

        // Mesurer la hauteur réelle des pages en arrière-plan
        startMeasurePageHeight(startIndex);
    }

    /**
     * Mesure les dimensions de l'image de référence dans un thread séparé,
     * puis met à jour toutes les hauteurs et scrolle vers la page de départ.
     */
    private void startMeasurePageHeight(final int startIndex) {
        if (imagePaths == null || imagePaths.isEmpty()) return;

        // Choisir une image de référence (la page de départ)
        final int refIdx = Math.min(startIndex, imagePaths.size() - 1);
        final String refPath = imagePaths.get(refIdx);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Juste les dimensions (très rapide)
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(refPath, opts);
                final int imgW = opts.outWidth;
                final int imgH = opts.outHeight;

                post(new Runnable() {
                    @Override
                    public void run() {
                        applyPageHeight(imgW, imgH, startIndex);
                    }
                });
            }
        }).start();
    }

    /**
     * Applique la hauteur calculée à tous les conteneurs de page,
     * puis scrolle vers startIndex et charge les premières pages.
     */
    private void applyPageHeight(int imgW, int imgH, final int startIndex) {
        int viewWidth = getWidth();
        if (viewWidth <= 0) {
            // Vue pas encore mesurée → réessayer un peu plus tard
            final int fw = imgW, fh = imgH;
            postDelayed(new Runnable() {
                @Override
                public void run() { applyPageHeight(fw, fh, startIndex); }
            }, 30);
            return;
        }

        // Calculer la hauteur en px
        if (imgW > 0 && imgH > 0) {
            pageHeightPx = viewWidth * imgH / imgW;
        } else {
            pageHeightPx = Math.round(viewWidth * QURAN_RATIO);
        }

        // Mettre à jour tous les conteneurs
        int n = (imagePaths != null) ? imagePaths.size() : 0;
        for (int i = 0; i < n; i++) {
            View pageContainer = container.getChildAt(i * 2);
            if (pageContainer == null) continue;
            LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) pageContainer.getLayoutParams();
            lp.height = pageHeightPx;
            pageContainer.setLayoutParams(lp);
        }

        // Demander une passe de mesure/layout
        container.requestLayout();

        // Scroller et charger après le prochain layout
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, startIndex * (pageHeightPx + sepPx));
                loadVisiblePages();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Navigation
    // ══════════════════════════════════════════════════════════════════════

    /** Scrolle vers la page d'index pagerIndex (0-based dans imagePaths). */
    public void scrollToIndex(int index, boolean smooth) {
        if (imagePaths == null || index < 0 || index >= imagePaths.size()) return;
        int targetY = index * (pageHeightPx + sepPx);
        if (smooth) smoothScrollTo(0, targetY);
        else scrollTo(0, targetY);
    }

    public void setPageChangeListener(PageChangeListener l) { this.listener = l; }
    public int getCurrentIndex() { return currentIndex; }

    // ══════════════════════════════════════════════════════════════════════
    // Scroll & chargement paresseux
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (pageHeightPx > 0) {
            loadVisiblePages();
            applyHighlight();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > 0 && pageHeightPx > 0) loadVisiblePages();
    }

    private void loadVisiblePages() {
        if (imagePaths == null || imagePaths.isEmpty() || pageHeightPx <= 0) return;
        int scrollY = getScrollY();
        int height  = getHeight();
        if (height <= 0) return;

        // Page au centre de l'écran
        int visible = indexAtY(scrollY + height / 2);
        if (visible != currentIndex) {
            currentIndex = visible;
            if (listener != null) listener.onPageChanged(visible);
        }

        int from = Math.max(0, visible - LOAD_RADIUS);
        int to   = Math.min(imagePaths.size() - 1, visible + LOAD_RADIUS);

        // Charger les pages dans le rayon
        for (int i = from; i <= to; i++) {
            if (!Boolean.TRUE.equals(loadTriggered.get(i))) {
                loadPage(i);
            }
        }

        // Décharger les pages hors rayon (évite OOM)
        for (int i = 0; i < imagePaths.size(); i++) {
            if (i < from - 1 || i > to + 1) {
                unloadPage(i);
            }
        }
    }

    /** Retourne l'index de la page à la coordonnée Y absolue (scroll-relative). */
    private int indexAtY(int y) {
        if (imagePaths == null || pageHeightPx <= 0) return 0;
        int stride = pageHeightPx + sepPx;
        int idx    = y / stride;
        return Math.max(0, Math.min(imagePaths.size() - 1, idx));
    }

    /** Déclenche le chargement de la page en arrière-plan (token anti-flickering). */
    private void loadPage(final int index) {
        if (imagePaths == null || index < 0 || index >= imagePaths.size()) return;
        final ImageView iv = imageViews.get(index);
        if (iv == null) return;

        // Marquer comme "déclenché" AVANT de lancer le thread
        loadTriggered.put(index, true);

        // Incrémenter le token : tout thread précédent pour ce même index sera ignoré
        final int token = loadToken.get(index, 0) + 1;
        loadToken.put(index, token);

        final String path = imagePaths.get(index);
        final int viewW   = getWidth();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Calculer inSampleSize pour ne pas charger plus de résolution que nécessaire
                BitmapFactory.Options opts = new BitmapFactory.Options();
                if (viewW > 0) {
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, opts);
                    if (opts.outWidth > 0) {
                        int sample = 1;
                        while (opts.outWidth / (sample * 2) >= viewW) sample *= 2;
                        opts.inSampleSize = sample;
                    }
                    opts.inJustDecodeBounds = false;
                }
                final Bitmap bmp = BitmapFactory.decodeFile(path, opts);

                post(new Runnable() {
                    @Override
                    public void run() {
                        // Vérifier que le token est toujours valide (pas eu de unload entre-temps)
                        if (loadToken.get(index, -1) == token) {
                            if (bmp != null) {
                                iv.setImageBitmap(bmp);
                            }
                        } else if (bmp != null) {
                            bmp.recycle(); // Token périmé : libérer la mémoire
                        }
                    }
                });
            }
        }).start();
    }

    /** Décharge la page (libère la mémoire) et invalide tout chargement en cours. */
    private void unloadPage(int index) {
        if (!Boolean.TRUE.equals(loadTriggered.get(index))) return; // déjà déchargée
        // Incrémenter le token : tout thread de chargement en cours sera ignoré
        loadToken.put(index, loadToken.get(index, 0) + 1);
        loadTriggered.put(index, false);
        ImageView iv = imageViews.get(index);
        if (iv != null) {
            // Recycler l'ancien bitmap
            android.graphics.drawable.Drawable d = iv.getDrawable();
            if (d instanceof android.graphics.drawable.BitmapDrawable) {
                Bitmap old = ((android.graphics.drawable.BitmapDrawable) d).getBitmap();
                if (old != null && !old.isRecycled()) old.recycle();
            }
            iv.setImageDrawable(null);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Surlignage tilawa
    // ══════════════════════════════════════════════════════════════════════

    public void setHighlight(int pagerIndex, int line, int point) {
        hlIndex = pagerIndex; hlLine = line; hlPoint = point;
    }

    public void clearHighlight() {
        hlIndex = -1;
        for (int i = 0; i < hlViews.size(); i++) {
            View hl = hlViews.valueAt(i);
            if (hl != null) hl.setVisibility(GONE);
        }
    }

    public void applyHighlight() {
        if (pageHeightPx <= 0) return;
        for (int i = 0; i < hlViews.size(); i++) {
            int  idx = hlViews.keyAt(i);
            View hl  = hlViews.valueAt(i);
            if (hl == null) continue;
            if (idx != hlIndex) {
                if (hl.getVisibility() != GONE) hl.setVisibility(GONE);
                continue;
            }
            ImageView iv = imageViews.get(idx);
            if (iv == null) continue;
            int w = iv.getWidth(), h = iv.getHeight();
            if (w <= 0 || h <= 0) { w = getWidth(); h = pageHeightPx; }
            if (w <= 0 || h <= 0) continue;

            int ln  = Math.max(1, Math.min(15, hlLine));
            int col = Math.max(0, Math.min(3, 4 - hlPoint));
            int cw  = w / 4, ch = h / 15;
            int left = col * cw;
            int top  = (ln - 1) * ch;

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(cw, ch);
            lp.leftMargin = left;
            lp.topMargin  = top;
            hl.setLayoutParams(lp);
            hl.setVisibility(VISIBLE);

            // Auto-scroll pour maintenir le surlignage visible
            int pageTop = idx * (pageHeightPx + sepPx);
            int absTop  = pageTop + top;
            int sy = getScrollY(), vh = getHeight();
            if (absTop < sy || absTop + ch > sy + vh) {
                smoothScrollTo(0, Math.max(0, absTop - vh / 3));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tap (affichage/masquage des barres de menu)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                tapStartX   = ev.getX();
                tapStartY   = ev.getY();
                tapDownTime = ev.getEventTime();
                break;

            case MotionEvent.ACTION_UP:
                float dx = Math.abs(ev.getX() - tapStartX);
                float dy = Math.abs(ev.getY() - tapStartY);
                long  dt = ev.getEventTime() - tapDownTime;
                // Tap reconnu seulement si : faible déplacement ET courte durée
                if (dx <= tapSlop && dy <= tapSlop && dt < TAP_MAX_MS && listener != null) {
                    float tapX = ev.getX();
                    float tapY = ev.getY() + getScrollY();
                    int w = getWidth(), h = pageHeightPx;
                    if (w > 0 && h > 0) {
                        int idx     = indexAtY((int) tapY);
                        int pageTop = idx * (h + sepPx);
                        float fx = Math.max(0f, Math.min(1f, tapX / w));
                        float fy = Math.max(0f, Math.min(1f, (tapY - pageTop) / h));
                        listener.onTap(fx, fy);
                    }
                }
                // Si c'était un scroll → rien (barres non affectées)
                break;
        }
        return super.onTouchEvent(ev);
    }
}
