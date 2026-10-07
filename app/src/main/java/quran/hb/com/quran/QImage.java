package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class QImage extends PagerAdapter {

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private LayoutInflater inflater;

    public QImage(Activity activity, ArrayList<String> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.q_image, container, false);

        ImageView imgDisplay = viewLayout.findViewById(R.id.imgDisplay);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        } catch (Exception e) {
            android.util.Log.e("QImage", "Erreur chargement image: " + _imagePaths.get(position));
        }

        if (bitmap != null) {
            imgDisplay.setImageBitmap(bitmap);
        } else {
            // Fichier manquant : afficher le nom du fichier attendu
            TextView txtErr = viewLayout.findViewById(R.id.txtPageNotFound);
            txtErr.setVisibility(android.view.View.VISIBLE);
            txtErr.setText("Fichier manquant :\n" + _imagePaths.get(position));
            imgDisplay.setVisibility(android.view.View.GONE);
        }


        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}
