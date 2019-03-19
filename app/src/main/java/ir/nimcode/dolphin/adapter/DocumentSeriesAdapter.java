package ir.nimcode.dolphin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.PropertyValues;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.OnItemClickListener;

/**
 * Created by saeed on 3/14/18.
 */

public class DocumentSeriesAdapter extends RecyclerView.Adapter<DocumentSeriesAdapter.DocumentSeriesViewHolder> {


    private Context context;
    private List<DocumentSeries> list;
    private OnItemClickListener onItemClickListener;

    public DocumentSeriesAdapter(Context context, List<DocumentSeries> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public DocumentSeriesAdapter.DocumentSeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DocumentSeriesAdapter.DocumentSeriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_document_series, parent, false));

    }

    @Override
    public void onBindViewHolder(DocumentSeriesAdapter.DocumentSeriesViewHolder holder, int position) {
        final int pos = position;

        holder.name.setText(String.format(new Locale("en"), "%s %d", "سری ", (position + 1)));


        holder.date.setText("تاریخ‌ : - ");

        final DocumentSeries documentSeries = list.get(pos);
        Property today = MyApplication.database.propertyDAO().getByTag("today");
        if (today != null) {
            PropertyValues todayValue = documentSeries.getMapValues().get(today.getId());
            if (todayValue != null) {
                String date = new JalaliCalendar(Long.parseLong(todayValue.getVal())).dateToString();
                holder.date.setText(date);
            }
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DocumentSeriesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout rootView;
        private TextView name;
        private TextView date;


        public DocumentSeriesViewHolder(View itemView) {
            super(itemView);

            this.rootView = itemView.findViewById(R.id.root_view);
            this.name = itemView.findViewById(R.id.name);
            this.date = itemView.findViewById(R.id.date);
        }
    }
}