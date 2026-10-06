package quran.hb.com.quran;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * adapter to populate listview with data
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class IndexJ_Connector extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> DjPage;
	private ArrayList<String> DjHizb;
	private ArrayList<String> DjAyaT;
	private ArrayList<String> DjDetail;
	int pageJ;
    String Hizb;
	public IndexJ_Connector(Context c, ArrayList<String> DjPage, ArrayList<String> DjHizb, ArrayList<String> DjAyaT, ArrayList<String> DjDetail) {
		this.mContext = c;

		this.DjPage = DjPage;
		this.DjHizb = DjHizb;
		this.DjAyaT = DjAyaT;
		this.DjDetail = DjDetail;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return DjPage.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int pos, View child, ViewGroup parent) {
		Holder mHolder;
		LayoutInflater layoutInflater;
		if (child == null) {
			layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			child = layoutInflater.inflate(R.layout.index_j_item, null);
			mHolder = new Holder();
			//mHolder.txt_DjPage = (TextView) child.findViewById(R.id.tv_page);
			mHolder.txt_DjHizb = (TextView) child.findViewById(R.id.tv_jz);
			mHolder.txt_DjAyaT = (TextView) child.findViewById(R.id.tv_aya);
			mHolder.txt_DjDetail = (TextView) child.findViewById(R.id.tv_detait);

			//if(mHolder.txt_DjPage.equals("11")){
			//	mHolder.txt_DjDetail.setTextColor(Color.parseColor("#444444"));
				//String mAN = cursor.getString(cursor.getColumnIndex("FANAME"));
				//mHolder.txt_DjDetail.setText(mAN);
			//}
		//	if((pos/7)-2==1) {
				//mHolder.txt_DjDetail.setTextColor(Color.parseColor("#04860b"));}
		//	    mHolder.txt_DjDetail.setBackgroundColor(Color.RED);}

			//mHolder.btn_BT = (Button) child.findViewById(R.id.btnHD);
			//mHolder.status.setText(getItem(position).status);
			//pageJ=Integer.parseInt(mHolder.txt_DjPage);

		//	pageJ =  Integer.parseInt( mHolder.txt_DjPage.getText().toString());
		//	if ( pageJ <9) {
		//		mHolder.txt_DjDetail.setBackgroundColor(Color.RED);
		//	} else {
		//		mHolder.txt_DjDetail.setBackgroundColor(Color.YELLOW);
		//	}

			child.setTag(mHolder);
		} else {
			mHolder = (Holder) child.getTag();
		}





		//mHolder.txt_DjPage.setText(DjPage.get(pos));
		//mHolder.txt_DjHizb.setText(DjHizb.get(pos));
		mHolder.txt_DjAyaT.setText(DjAyaT.get(pos));
		mHolder.txt_DjDetail.setText(DjDetail.get(pos));
		//mHolder.btn_BT.setText(Btn.get(pos));
		//if(pos==7) {
		//mHolder.txt_DjDetail.setBackgroundColor(Color.RED);}
		mHolder.txt_DjHizb.setPaintFlags(mHolder.txt_DjHizb.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		if(pos%8==0)
		//if (pos < mHolder.txt_DjPage.length())
		{
		//	mHolder.txt_DjPage.setText(DjPage.get(pos));
			mHolder.txt_DjHizb.setText("ط¬ط²ط، "+(1+(pos/8))+"    "+"ط­ط²ط¨ "+(1+(pos/4)));
		//	mHolder.txt_DjAyaT.setText(DjAyaT.get(pos));
		//	mHolder.txt_DjDetail.setText(DjDetail.get(pos));

			child.setBackgroundColor(Color.BLACK);
			mHolder.txt_DjHizb.setBackgroundColor(Color.parseColor("#000000"));
			mHolder.txt_DjHizb.setTextColor(Color.parseColor("#ffffff"));
			mHolder.txt_DjAyaT.setTextColor(Color.parseColor("#ffffff"));
			mHolder.txt_DjDetail.setTextColor(Color.parseColor("#ffffff"));

			//TextView txt = (TextView) findViewById(R.id.Textview1);
			//mHolder.txt_DjHizb.setPaintFlags(mHolder.txt_DjHizb.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			//Hizb = mHolder.txt_DjHizb.toString();
			//Hizb.substring(0,4);
			//mHolder.txt_DjHizb.setText(Hizb);
		}else if(pos%4==0){

		//	mHolder.txt_DjPage.setText(DjPage.get(pos));
			mHolder.txt_DjHizb.setText("ط­ط²ط¨ "+(1+(pos/4)));
		//	mHolder.txt_DjAyaT.setText(DjAyaT.get(pos));
		//	mHolder.txt_DjDetail.setText(DjDetail.get(pos));

			child.setBackgroundColor(Color.parseColor("#dfdada"));
			mHolder.txt_DjHizb.setBackgroundColor(Color.parseColor("#000000"));
			mHolder.txt_DjHizb.setTextColor(Color.parseColor("#ffffff"));
			mHolder.txt_DjAyaT.setTextColor(Color.parseColor("#000000"));
			mHolder.txt_DjDetail.setTextColor(Color.parseColor("#000000"));

		}else {

		//	mHolder.txt_DjPage.setText(DjPage.get(pos));
			mHolder.txt_DjHizb.setText(DjHizb.get(pos));
		//	mHolder.txt_DjAyaT.setText(DjAyaT.get(pos));
		//	mHolder.txt_DjDetail.setText(DjDetail.get(pos));

			child.setBackgroundColor(Color.parseColor("#dfdada"));
			mHolder.txt_DjHizb.setBackgroundColor(Color.parseColor("#dfdada"));
			mHolder.txt_DjHizb.setTextColor(Color.parseColor("#000000"));
			mHolder.txt_DjAyaT.setTextColor(Color.parseColor("#000000"));
			mHolder.txt_DjDetail.setTextColor(Color.parseColor("#000000"));

		}




		return child;

	}

	public class Holder {
		TextView txt_DjPage;
		TextView txt_DjHizb;
		TextView txt_DjAyaT;
		TextView txt_DjDetail;
		//Button btn_BT;
	}

}

