package quran.hb.com.quran;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class About extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView txt02 = (TextView) findViewById(R.id.txt02);
        TextView txt031 = (TextView) findViewById(R.id.txt031);
        AddressOnClickListners((TextView) findViewById(R.id.txt041), "http://facebook.com/benaderh.hb.1");
        txt031.setOnClickListener(new txt031C());
        txt02.setText("ظ‡ط´ط§ظ…  ط¨ظ† ظ†ط§ط¯ط± - ط¹ظٹظ† ط§ظ„ط¨ظٹط¶ط§ط، - ط§ظ„ط¬ط²ط§ط¦ط±");
    }

    class txt031C implements OnClickListener {
        txt031C() {
        }
        public void onClick(View view) {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("plain/text");
            intent.putExtra("android.intent.extra.EMAIL", new String[]{"benaderh@gmail.com"});
            About.this.startActivity(intent);
        }
    }

    private void AddressOnClickListners(TextView tv, String url) {
        tv.setOnClickListener(new txt041C(url));
    }

    class txt041C implements OnClickListener {
        private final String val$url;

        txt041C(String str) {
            this.val$url = str;
        }

        public void onClick(View view) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(this.val$url));
            About.this.startActivity(intent);
        }
    }



    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            //SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
            //SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
            this.finish();
            startActivity(new Intent(this, QMain.class));
            return true;
        }
        //this.finish();
        return super.onKeyUp( keyCode, event );
    }



}

