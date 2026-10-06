package quran.hb.com.quran;

import android.content.Context;
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
public class IndexP_Connector extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> Page;
	private ArrayList<String> Sora;
	//private ArrayList<String> NJr;
	

	public IndexP_Connector(Context c, ArrayList<String> Page, ArrayList<String> Sora) {
		this.mContext = c;

		this.Page = Page;
		this.Sora = Sora;
		//this.NJr = NJr;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return Page.size();
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
			child = layoutInflater.inflate(R.layout.index_p_item, null);
			mHolder = new Holder();
			mHolder.txt_Page = (TextView) child.findViewById(R.id.tv_page);
			mHolder.txt_Sora = (TextView) child.findViewById(R.id.tv_sora);
			//mHolder.txt_NJ = (TextView) child.findViewById(R.id.tv_nj);
			//mHolder.btn_BT = (Button) child.findViewById(R.id.btnHD);
			child.setTag(mHolder);
		} else {
			mHolder = (Holder) child.getTag();
		}
		mHolder.txt_Page.setText(Page.get(pos));
		mHolder.txt_Sora.setText(Sora.get(pos));
		//mHolder.txt_NJ.setText(NJr.get(pos));
		//mHolder.btn_BT.setText(Btn.get(pos));


		return child;
	}

	public class Holder {
		TextView txt_Page;
		TextView txt_Sora;
		//TextView txt_NJ;
		//Button btn_BT;
	}

}

