package ir.nimcode.dolphin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.adapter.DocumentAdapter;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.OnItemClickListener;
import ir.nimcode.dolphin.util.Utilities;

public class HistoryActivity extends FullAppCompatActivity {

    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.history)));

        final List<Document> documents = MyApplication.database.documentDAO().getAllHistory();
        DocumentAdapter documentAdapter = new DocumentAdapter(HistoryActivity.this, documents, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Document document = documents.get(position);
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                intent.putExtra("latitude", document.getLatitude());
                intent.putExtra("longitude", document.getLongitude());
                finish();
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
        recyclerView.setAdapter(documentAdapter);
        documentAdapter.notifyDataSetChanged();
        showNotingFoundErrorLayout(documents.isEmpty());
    }

    public void showNotingFoundErrorLayout(boolean visibility) {
        notingFoundErrorLayout.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }
}
