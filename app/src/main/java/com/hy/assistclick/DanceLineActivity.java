package com.hy.assistclick;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hy.assistclick.danceline.DanceLine;
import com.hy.assistclick.danceline.DanceLineListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author hy
 * @date 2023/1/10
 * desc: DanceLineActivity
 **/
public class DanceLineActivity extends AppCompatActivity {

    private TextView tvClick;
    private RecyclerView recyclerView;

    private DanceLineListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_line);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getString(R.string.dance_line));
        }

        tvClick = findViewById(R.id.tv_click);
        recyclerView = findViewById(R.id.rv_dance_line);

        List<DanceLine> dataList = buildDanceLineData();
        adapter = new DanceLineListAdapter(dataList);
        recyclerView.setAdapter(adapter);

        tvClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = new Random().nextInt(200);
                int y = new Random().nextInt(600);
                if (BuildConfig.DEBUG) {
                    Log.i("AutoClick", "x:" + x + " y:" + y);
                }
                tvClick.setText("点击屏幕:" + "x:" + x + " y:" + y);
            }
        });

        // new Handler().postDelayed(new Runnable() {
        //     @Override
        //     public void run() {
        //         autoClick();
        //     }
        // }, 1000);
    }

    private List<DanceLine> buildDanceLineData() {
        List<DanceLine> dataList = new ArrayList<>();
        DanceLine line1 = new DanceLine();
        line1.setName("春天");
        dataList.add(line1);

        DanceLine line2 = new DanceLine();
        line2.setName("钢琴");
        dataList.add(line2);

        int i = 30;
        for (int i1 = 0; i1 < i; i1++) {
            DanceLine line = new DanceLine();
            line.setName("name" + i1);
            dataList.add(line);
        }
        return dataList;
    }

    public void autoClick() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AssistService.getInstance() != null) {
                    AssistService.getInstance().dispatchClick(400, 500);
                }
                autoClick();
            }
        }, 1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}