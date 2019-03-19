package ir.nimcode.dolphin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ldoublem.loadingviewlib.view.LVNews;

import java.util.List;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Form;

import static ir.nimcode.dolphin.activity.FormsActivity.isParentShow;

/**
 * Created by saeed on 11/26/17.
 */

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormViewHolder> {


    private Context context;
    private List<Form> list;
    private LVNews progress;
    private LinearLayout notingFoundErrorLayout;

    public FormAdapter(Context context, List<Form> list, LVNews progress, LinearLayout notingFoundErrorLayout) {
        this.context = context;
        this.list = list;
        this.progress = progress;
        this.notingFoundErrorLayout = notingFoundErrorLayout;
    }

    @Override
    public FormAdapter.FormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FormViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_form, parent, false));

    }

    @Override
    public void onBindViewHolder(FormAdapter.FormViewHolder holder, int position) {
        final Form form = list.get(position);
        if (!form.isVisibility()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }
        holder.name.setText(form.getName_fa());
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (form.getParent_id() == 0) {

                    notingFoundErrorLayout.setVisibility(View.INVISIBLE);

                    list.clear();
                    notifyDataSetChanged();

                    progress.setVisibility(View.VISIBLE);
                    progress.startAnim(1000);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isParentShow = false;
                            list.addAll(MyApplication.database.formDAO().getAll(form.getId()));
                            progress.stopAnim();
                            progress.setVisibility(View.INVISIBLE);
                            notifyDataSetChanged();
                            if (list.isEmpty()) {
                                notingFoundErrorLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }, 1000);
                } else {

                    Intent intent = new Intent(context, SheetActivity.class);
                    intent.putExtra("form_id", form.getId());
                    intent.putExtra("form_name", form.getName_fa());
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.right_in, R.anim.right_out);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FormViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout rootView;
        private TextView name;


        public FormViewHolder(View itemView) {
            super(itemView);

            this.name = itemView.findViewById(R.id.name);
            this.rootView = itemView.findViewById(R.id.root_view);
        }
    }
}
