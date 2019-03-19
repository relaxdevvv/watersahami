package ir.nimcode.dolphin.activity;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ldoublem.loadingviewlib.view.LVNews;

import java.util.ArrayList;
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

public class SearchResultActivity extends FullAppCompatActivity {

    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress)
    LVNews progress;
    private List<Document> documents;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.search_result)));

        intent = getIntent();

        progress.setViewColor(getResources().getColor(R.color.md_grey_800));
        progress.setVisibility(View.VISIBLE);
        progress.startAnim(1000);

        documents = new ArrayList<>();
        final DocumentAdapter documentAdapter = new DocumentAdapter(SearchResultActivity.this, documents, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Document document = documents.get(position);
                setResult(RESULT_OK, intent);
                intent.putExtra("latitude", document.getLatitude());
                intent.putExtra("longitude", document.getLongitude());
                finish();
            }
        });
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchResultActivity.this));
        recyclerView.setAdapter(documentAdapter);

        SupportSQLiteQuery query = new SimpleSQLiteQuery(intent.getStringExtra("query"));
        Log.d("TEST", "onCreate: " + query.getSql());
        try {
            Cursor cursor = MyApplication.database.query(query);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        documents.add(MyApplication.database.documentDAO().get(cursor.getLong(1)));
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.INVISIBLE);
                progress.stopAnim();
                showNotingFoundErrorLayout(documents.isEmpty());
                documentAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        }, 1000);

    }

    public void showNotingFoundErrorLayout(boolean visibility) {
        notingFoundErrorLayout.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }
}
