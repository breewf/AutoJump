package com.hy.assistclick;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author hy
 * @date 2020/5/9
 * ClassDesc:HelpActivity.
 **/
public class HelpActivity extends AppCompatActivity {

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            // 主标题
            mActionBar.setTitle(getString(R.string.help_title));
            // mActionBar.setDisplayHomeAsUpEnabled(true);
            // mActionBar.setHomeButtonEnabled(true);
        }
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