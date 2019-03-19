package ir.nimcode.dolphin.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marcouberti.autofitbutton.AutoFitButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.OnItemClickListener;

/**
 * Created by saeed on 3/9/18.
 */

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {


    private Context context;
    private List<Document> list;
    private OnItemClickListener mListener;

    public DocumentAdapter(Context context, List<Document> list, OnItemClickListener mListener) {
        this.context = context;
        this.list = list;
        this.mListener = mListener;
    }

    @Override
    public DocumentAdapter.DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DocumentAdapter.DocumentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_document, parent, false));

    }

    @Override
    public void onBindViewHolder(final DocumentAdapter.DocumentViewHolder holder, final int position) {
        final Document document = list.get(position);
        holder.formName.setText(MyApplication.database.formDAO().get(document.getForm_id()).getName_fa());
        List<DocumentSeries> documentSeries = MyApplication.database.documentSeriesDAO().getAll(document.getId());
        if (documentSeries.size() > 0 && documentSeries.get(documentSeries.size() - 1).getMapValues().containsKey(1L)) {
            holder.ownerCode.setText(documentSeries.get(documentSeries.size() - 1).getMapValues().get(1L).getVal());
        }
        if (document.getLast_seen_date() != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(document.getLast_seen_date());
            holder.lastSeenDate.setText(new JalaliCalendar(cal).toString());
        } else {
            holder.lastSeenDate.setText("-");
        }
        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SheetActivity.class);
                intent.putExtra("document_id", document.getId());
                intent.putExtra("form_id", document.getForm_id());
                context.startActivity(intent);
                document.setLast_seen_date(new Date().getTime());
                MyApplication.database.documentDAO().update(document);
            }
        });
        holder.showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class DocumentViewHolder extends RecyclerView.ViewHolder {

        private TextView formName;
        private TextView ownerCode;
        private TextView lastSeenDate;
        private AutoFitButton showOnMap;
        private AutoFitButton details;


        public DocumentViewHolder(View itemView) {
            super(itemView);

            this.formName = itemView.findViewById(R.id.form_name);
            this.ownerCode = itemView.findViewById(R.id.owner_code);
            this.lastSeenDate = itemView.findViewById(R.id.last_seen_date);
            this.showOnMap = itemView.findViewById(R.id.show_on_map);
            this.details = itemView.findViewById(R.id.details);
        }

    }
}