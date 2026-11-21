package quran.hb.com.quran;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager qui désactive le glissement gauche/droite en mode paysage (landscape).
 * En mode paysage, la navigation se fait par le scroll vertical de PageScrollView.
 */
public class NoSwipeViewPager extends ViewPager {

    public NoSwipeViewPager(Context context) {
        super(context);
    }

    public NoSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isLandscape()) {
            return false; // ne pas intercepter les touches en paysage
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isLandscape()) {
            return false; // ne pas consommer les touches en paysage
        }
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
