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
public class IndexS_Connector extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> SoraI;
	private ArrayList<String> SoraT;
	private ArrayList<String> SoraP;
	

	public IndexS_Connector(Context c, ArrayList<String> SoraI, ArrayList<String> SoraT, ArrayList<String> SoraP) {
		this.mContext = c;

		this.SoraI = SoraI;
		this.SoraT = SoraT;
		this.SoraP = SoraP;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return SoraI.size();
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
			child = layoutInflater.inflate(R.layout.index_s_item, null);
			mHolder = new Holder();
			mHolder.txt_SoraI = (TextView) child.findViewById(R.id.tv_soraI);
			mHolder.txt_SoraT = (TextView) child.findViewById(R.id.tv_soraT);
			mHolder.txt_SoraP = (TextView) child.findViewById(R.id.tv_soraP);
			//mHolder.btn_BT = (Button) child.findViewById(R.id.btnHD);
			child.setTag(mHolder);
		} else {
			mHolder = (Holder) child.getTag();
		}
		mHolder.txt_SoraI.setText(SoraI.get(pos));
		mHolder.txt_SoraT.setText(SoraT.get(pos));
		mHolder.txt_SoraP.setText(SoraP.get(pos));
		//mHolder.btn_BT.setText(Btn.get(pos));


		return child;
	}

	public class Holder {
		TextView txt_SoraI;
		TextView txt_SoraT;
		TextView txt_SoraP;
		//Button btn_BT;
	}

}
