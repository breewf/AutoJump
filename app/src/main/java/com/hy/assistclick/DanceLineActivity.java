package com.hy.assistclick;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author hy
 * @date 2023/1/10
 * desc: DanceLineActivity
 **/
public class DanceLineActivity extends AppCompatActivity {

    private TextView tvClick;

    private AssistService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_line);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getString(R.string.dance_line));
        }

        tvClick = findViewById(R.id.tv_click);

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                autoClick();
            }
        }, 1000);
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