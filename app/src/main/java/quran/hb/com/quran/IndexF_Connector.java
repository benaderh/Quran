package quran.hb.com.quran;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Juned on 2/20/2017.
 */

public class IndexF_Connector extends ArrayAdapter<IndexF_Variables> {

    public ArrayList<IndexF_Variables> MainList;

    public ArrayList<IndexF_Variables> KranListTemp;

    public IndexF_Connector.SubjectDataFilter kranDataFilter;

    public IndexF_Connector(Context context, int id, ArrayList<IndexF_Variables> kranArrayList) {

        super(context, id, kranArrayList);

        this.KranListTemp = new ArrayList<IndexF_Variables>();

        this.KranListTemp.addAll(kranArrayList);

        this.MainList = new ArrayList<IndexF_Variables>();

        this.MainList.addAll(kranArrayList);
    }

    @Override
    public Filter getFilter() {

        if (kranDataFilter == null) {

            kranDataFilter = new IndexF_Connector.SubjectDataFilter();
        }
        return kranDataFilter;
    }


    public class ViewHolder {

        //TextView Name;
        //TextView Number;
        TextView tv_sora;
        TextView tv_ayaI;
        TextView tv_ayaT;
        TextView tv_pageI;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        IndexF_Connector.ViewHolder holder = null;

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInflater.inflate(R.layout.index_f_item, null);

            holder = new IndexF_Connector.ViewHolder();

            holder.tv_sora = (TextView) convertView.findViewById(R.id.tv_sora);
            holder.tv_ayaI = (TextView) convertView.findViewById(R.id.tv_ayaI);
            holder.tv_ayaT = (TextView) convertView.findViewById(R.id.tv_ayaT);
            holder.tv_pageI = (TextView) convertView.findViewById(R.id.tv_pageI);

            convertView.setTag(holder);

        } else {

            holder = (IndexF_Connector.ViewHolder) convertView.getTag();
        }

        IndexF_Variables kran = KranListTemp.get(position);

        holder.tv_sora.setText(kran.getSora());
        holder.tv_ayaI.setText("ط§ظ„ط¢ظٹط©: " + kran.getAyai());
        holder.tv_ayaT.setText(kran.getAyat());
        holder.tv_pageI.setText("ط§ظ„طµظپط­ط©: " + kran.getPage());

        return convertView;

    }

    private class SubjectDataFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            charSequence = charSequence.toString().toLowerCase();

            FilterResults filterResults = new FilterResults();

            if (charSequence != null && charSequence.toString().length() > 0) {

                ArrayList<IndexF_Variables> arrayList1 = new ArrayList<IndexF_Variables>();

                for (int i = 0, l = MainList.size(); i < l; i++) {
                    IndexF_Variables subject = MainList.get(i);

                    if (subject.toString().toLowerCase().contains(charSequence))

                        arrayList1.add(subject);
                }
                filterResults.count = arrayList1.size();

                filterResults.values = arrayList1;
            } else {
                synchronized (this) {
                    filterResults.values = MainList;

                    filterResults.count = MainList.size();
                }
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            KranListTemp = (ArrayList<IndexF_Variables>) filterResults.values;

            notifyDataSetChanged();

            clear();

            for (int i = 0, l = KranListTemp.size(); i < l; i++)
                add(KranListTemp.get(i));

            notifyDataSetInvalidated();
        }
    }
}
