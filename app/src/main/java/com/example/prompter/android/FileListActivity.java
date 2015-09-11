package com.example.prompter.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.prompter.android.util.FileUtils;

import java.io.File;

public class FileListActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "FileListActivity";

    private static final int LOADER_ID_ASSET_COPY = 0;
    private static final int LOADER_ID_FILE_LIST = 1;

    private File mDir;

    private ListView mListView;

    private String[] mFileList = new String[0];

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mFileList.length;
        }

        @Override
        public Object getItem(int position) {
            return mFileList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String fileName = (String) getItem(position);

            TextView textView = (TextView) View.inflate(FileListActivity.this, R.layout.row_file_list, null);
            textView.setText(fileName);

            return textView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDir = FileUtils.getDir(this);

        setContentView(R.layout.activity_list);

        TextView hint = (TextView) findViewById(R.id.tv_hint);
        hint.setText(mDir.getAbsolutePath());

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(LOADER_ID_ASSET_COPY, null, mFileCopyLoaderCallback);
    }

    private final LoaderManager.LoaderCallbacks<AssetCopyLoader.Result> mFileCopyLoaderCallback
            = new LoaderManager.LoaderCallbacks<AssetCopyLoader.Result>() {

        @Override
        public Loader<AssetCopyLoader.Result> onCreateLoader(int id, Bundle args) {
            AssetCopyLoader loader = new AssetCopyLoader(FileListActivity.this, mDir);
            loader.forceLoad();

            return loader;
        }

        @Override
        public void onLoadFinished(Loader<AssetCopyLoader.Result> loader, AssetCopyLoader.Result data) {
            getLoaderManager().restartLoader(LOADER_ID_FILE_LIST, null, mFileListLoaderCallback);
        }

        @Override
        public void onLoaderReset(Loader<AssetCopyLoader.Result> loader) {

        }
    };

    private final LoaderManager.LoaderCallbacks<String[]> mFileListLoaderCallback
            = new LoaderManager.LoaderCallbacks<String[]>() {

        @Override
        public Loader<String[]> onCreateLoader(int id, Bundle args) {
            FileListLoader loader = new FileListLoader(FileListActivity.this, mDir);
            loader.forceLoad();

            return loader;
        }

        @Override
        public void onLoadFinished(Loader<String[]> loader, String[] data) {
            if (data == null) {
                return;
            }

            mFileList = data;
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<String[]> loader) {
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String fileName = (String) mAdapter.getItem(position);

        Intent intent = ScrollActivity.newIntent(this, fileName);
        startActivity(intent);
    }

    private static class FileListLoader extends AsyncTaskLoader<String[]> {

        private final File dir;

        public FileListLoader(Context context, File dir) {
            super(context);
            this.dir = dir;
        }

        @Override
        public String[] loadInBackground() {
            return dir.list();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "KeyCode = " + event.getKeyCode());

        int position = mListView.getSelectedItemPosition();

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                onItemClick(mListView, mListView.getSelectedView(), position, mAdapter.getItemId(position));
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: // FORWARD
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT: // BACK
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP: {
                position--;
                position = position < 0 ? 0 : position;
                mListView.setSelection(position);
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                position++;
                position = position > mListView.getCount() - 1 ? mListView.getCount() - 1 : position;
                mListView.setSelection(position);
                return true;
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                onItemClick(mListView, mListView.getSelectedView(), position, mAdapter.getItemId(position));
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                return true;
//            case KeyEvent.KEYCODE_MEDIA_EJECT:
//                finish();

        }
        return super.onKeyDown(keyCode, event);
    }

}
