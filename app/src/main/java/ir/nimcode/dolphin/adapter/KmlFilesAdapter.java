package ir.nimcode.dolphin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.KmlFilesActivity;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.KmlFile;

/**
 * Created by saeed on 3/8/18.
 */

public class KmlFilesAdapter extends RecyclerView.Adapter<KmlFilesAdapter.KmlFileViewHolder> {


    private Context context;
    private List<KmlFile> list;
    private SweetAlertDialog progressDialog;

    public KmlFilesAdapter(Context context, List<KmlFile> list) {
        this.context = context;
        this.list = list;

        progressDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        progressDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.colorAccent));
        progressDialog.setTitleText(context.getString(R.string.warning));
        progressDialog.setContentText(context.getString(R.string.delete_file_question));
        progressDialog.setCancelText(context.getString(R.string.no));
        progressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                progressDialog.dismiss();
            }
        });
        progressDialog.setCancelable(false);
    }

    @Override
    public KmlFilesAdapter.KmlFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new KmlFileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_kml_file, parent, false));

    }

    @Override
    public void onBindViewHolder(final KmlFilesAdapter.KmlFileViewHolder holder, int position) {
        final int pos = position;
        final KmlFile kmlFile = list.get(position);
        holder.name.setText(kmlFile.getName());
        holder.visibility.setImageDrawable(context.getResources().getDrawable(kmlFile.isVisibility() ? R.drawable.ic_visibility : R.drawable.ic_visibility_off));
        holder.visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kmlFile.setVisibility(!kmlFile.isVisibility());
                holder.visibility.setImageDrawable(context.getResources().getDrawable(kmlFile.isVisibility() ? R.drawable.ic_visibility : R.drawable.ic_visibility_off));
                MyApplication.database.kmlLayerDAO().update(kmlFile);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setConfirmText(context.getString(R.string.yes));
                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                        list.remove(kmlFile);
                        notifyItemRemoved(pos);
                        MyApplication.database.kmlLayerDAO().remove(kmlFile.getId());
                        ((KmlFilesActivity) context).showNotingFoundErrorLayout(list.isEmpty());
                        File file = new File(kmlFile.getUrl());
                        if (file.exists()) {
                            file.delete();
                        }

                    }
                });
                progressDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class KmlFileViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private FloatingActionButton visibility;
        private FloatingActionButton delete;


        public KmlFileViewHolder(View itemView) {
            super(itemView);

            this.name = itemView.findViewById(R.id.name);
            this.visibility = itemView.findViewById(R.id.visibility);
            this.delete = itemView.findViewById(R.id.delete);
        }

    }
}