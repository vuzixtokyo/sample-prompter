package com.example.prompter.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prompter.android.util.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;

public class ScrollActivity extends Activity
        implements LoaderManager.LoaderCallbacks<FileLoader.Result> {
    private static final String TAG = "ScrollActivity";

    private static final String FONT_FILE_NAME = "NotoSansJP-Regular.otf";

    private static final String KEY_FILE_NAME = "key_file_name";
    private static final int LOADER_ID_FILE_LOAD = 0;

    private File mDir;

    private volatile int speedLevel = 40;

    private ScrollView mScrollView;
    private TextView mTextView;

    private Handler mHandler;

    public static Intent newIntent(Context context, String fileName) {
        Intent intent = new Intent(context, ScrollActivity.class);
        intent.putExtra(KEY_FILE_NAME, fileName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mDir = FileUtils.getDir(this);

        mHandler = new Handler(this);

        setContentView(R.layout.activity_scroll);

        mTextView = (TextView) findViewById(R.id.tv_overview);
        mScrollView = (ScrollView) findViewById(R.id.sv_root);

        Typeface typeface = Typeface.createFromAsset(getAssets(), FONT_FILE_NAME);
        mTextView.setTypeface(typeface);

        mHandler.sendEmptyMessageDelayed(HANDLE_AUTO_SCROLL, getSpeed());

        String fileName = getIntent().getStringExtra(KEY_FILE_NAME);

        if (fileName == null) {
            Toast.makeText(this, "Error: missing file name.", Toast.LENGTH_LONG).show();
            finish();
        }
        Bundle args = new Bundle();
        args.putString(KEY_FILE_NAME, fileName);

        getLoaderManager().initLoader(LOADER_ID_FILE_LOAD, args, this);
    }

    private static final int HANDLE_AUTO_SCROLL = 0x01;

    private int getSpeed() {
        return 1000 / speedLevel;

    }

    @Override
    public Loader<FileLoader.Result> onCreateLoader(int id, Bundle args) {

        String fileName = args.getString(KEY_FILE_NAME);

        FileLoader loader = new FileLoader(this, new File(mDir, fileName));
        loader.forceLoad();

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<FileLoader.Result> loader, FileLoader.Result data) {
        if (data.exception != null) {
            Toast.makeText(this, data.exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mTextView.setText(data.data);
    }

    @Override
    public void onLoaderReset(Loader<FileLoader.Result> loader) {
    }

    private static class Handler extends android.os.Handler {
        private final WeakReference<ScrollActivity> activity;

        Handler(ScrollActivity a) {
            activity = new WeakReference<ScrollActivity>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            ScrollActivity a = activity.get();
            if (a == null) {
                return;
            }

            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_AUTO_SCROLL:
                    if (!a.isPaused()) {
                        a.mScrollView.scrollBy(0, 2);
                    }
                    sendEmptyMessageDelayed(HANDLE_AUTO_SCROLL, a.getSpeed());
                    break;
            }
        }
    }

    private void smoothScrollBy(int y) {
        isPaused = true;
        mScrollView.smoothScrollBy(0, y);
    }

    private static final int SCROOL_VALUE = 100;

    private static final int SCROOL_LEVEL_INTERVAL = 10;
    private static final int MIN_SPEED_LEVEL = 10;
    private static final int MAX_SPEED_LEVEL = 500;

    private void changeSpeed(int value) {
        int s = speedLevel + value;
        if (s > MAX_SPEED_LEVEL) {
            s = MAX_SPEED_LEVEL;
        }
        if (s < MIN_SPEED_LEVEL) {
            s = MIN_SPEED_LEVEL;
        }
        speedLevel = s;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "KeyCode = " + event.getKeyCode());

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                isPaused = !isPaused;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: // FORWARD
                smoothScrollBy(SCROOL_VALUE);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT: // BACK
                smoothScrollBy(-SCROOL_VALUE);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP: {
                changeSpeed(SCROOL_LEVEL_INTERVAL);
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                changeSpeed(-SCROOL_LEVEL_INTERVAL);
                return true;
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                isPaused = !isPaused;
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                smoothScrollBy(-SCROOL_VALUE);
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                smoothScrollBy(SCROOL_VALUE);
                return true;
            case KeyEvent.KEYCODE_MEDIA_EJECT:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isPaused = true;

    private boolean isPaused() {
        return isPaused;
    }
}
