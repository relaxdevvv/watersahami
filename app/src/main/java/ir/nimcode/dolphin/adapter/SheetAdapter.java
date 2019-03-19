package ir.nimcode.dolphin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.model.Sheet;

/**
 * Created by saeed on 11/26/17.
 */

public class SheetAdapter extends RecyclerView.Adapter<SheetAdapter.SheetViewHolder> {


    private Context context;
    private List<Sheet> list;

    public SheetAdapter(Context context, List<Sheet> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public SheetAdapter.SheetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SheetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_form, parent, false));

    }

    @Override
    public void onBindViewHolder(SheetAdapter.SheetViewHolder holder, final int position) {
        final Sheet sheet = list.get(position);
        holder.setVisibility(sheet.isVisibility());
        holder.name.setText(sheet.getName_fa());
        holder.count.setText("");
        holder.allChecked.setVisibility(View.GONE);
        if (sheet.getProperties_error_count() != 0) {
            holder.countError.setText(sheet.getProperties_error_count() + "");
        } else {
            holder.countError.setText("");
        }
        if (sheet.getProperties_checked_count() == sheet.getProperties_count()) {
            holder.allChecked.setVisibility(View.VISIBLE);
        } else if (sheet.getProperties_checked_count() != 0) {
            holder.count.setText(String.format(new Locale("en"), "%d/%d", sheet.getProperties_count(), sheet.getProperties_checked_count()));
        }
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SheetActivity) context).setCurrentItem(position + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SheetViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout rootView;
        private TextView name;
        private TextView count;
        private TextView countError;
        private ImageView allChecked;


        public SheetViewHolder(View itemView) {
            super(itemView);

            this.name = itemView.findViewById(R.id.name);
            this.count = itemView.findViewById(R.id.count);
            this.countError = itemView.findViewById(R.id.count_error);
            this.rootView = itemView.findViewById(R.id.root_view);
            this.allChecked = itemView.findViewById(R.id.all_checked);
        }

        public void setVisibility(boolean isVisible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (isVisible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                itemView.setVisibility(View.VISIBLE);
            } else {
                itemView.setVisibility(View.GONE);
                param.height = 0;
                param.width = 0;
            }
            itemView.setLayoutParams(param);
        }
    }
}
