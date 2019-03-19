package ir.nimcode.dolphin.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ldoublem.loadingviewlib.view.LVNews;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.adapter.FormAdapter;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Form;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;

public class FormsActivity extends FullAppCompatActivity {

    public static boolean isParentShow = true;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress)
    LVNews progress;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    private ArrayList<Form> forms;
    private FormAdapter formAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);
        ButterKnife.bind(this);

        forms = new ArrayList<>();
        formAdapter = new FormAdapter(FormsActivity.this, forms, progress, notingFoundErrorLayout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(FormsActivity.this));
        recyclerView.setAdapter(formAdapter);

        progress.setViewColor(getResources().getColor(R.color.md_grey_800));

        Form form = MyApplication.database.formDAO().get(MyApplication.sp.getFilterFormId());
        if (form == null) {
            Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.forms)));
            getForms(0L);
        } else {
            Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.forms) + " > " + form.getName_fa()));
            getForms(form.getId());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.showOfflineMode(FormsActivity.this, !Utilities.isAvailableNetwork(FormsActivity.this));
    }

    private void getForms(final long parentId) {

        notingFoundErrorLayout.setVisibility(View.INVISIBLE);

        forms.clear();
        formAdapter.notifyDataSetChanged();

        progress.setVisibility(View.VISIBLE);
        progress.startAnim(1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                forms.addAll(MyApplication.database.formDAO().getAll(parentId));
                progress.stopAnim();
                progress.setVisibility(View.INVISIBLE);
                formAdapter.notifyDataSetChanged();
                if (forms.isEmpty()) {
                    notingFoundErrorLayout.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

    }

    @Override
    public void onBackPressed() {
        if (!isParentShow) {
            isParentShow = true;
            getForms(0);
        } else {
            super.onBackPressed();
        }
    }
}
