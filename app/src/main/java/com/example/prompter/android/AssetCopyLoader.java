package com.example.prompter.android;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetCopyLoader extends AsyncTaskLoader<AssetCopyLoader.Result> {

    private static final String TAG = "AssetCopyLoader";
    private final File destDir;

    public AssetCopyLoader(Context context, File destDir) {
        super(context);

        this.destDir = destDir;
    }

    @Override
    public Result loadInBackground() {
        Log.d(TAG, "loadInBackground " + destDir.getAbsolutePath());

        Exception exception = null;

        try {
            String[] fileList = getContext().getAssets().list("");

            if (fileList.length != 0 && !destDir.exists()) {
                destDir.mkdirs();
            }

            for (String fileName : fileList) {

                OutputStream outputStream = null;
                InputStream inputStream = null;

                try {
                    File out = new File(destDir, fileName);
                    if (out.exists() || out.isDirectory() || fileName.lastIndexOf(".txt") == -1) {
                        continue;
                    }

                    if (!out.exists()) {
                        out.createNewFile();
                    }

                    outputStream = new FileOutputStream(out);
                    inputStream = getContext().getAssets().open(fileName);

                    IOUtils.copy(inputStream, outputStream);

                } catch (IOException e) {
                    Log.e(TAG, "IOException", e);
                    exception = e;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }

        } catch (IOException e) {
            exception = e;
        }

        return new Result(exception);
    }

    public static class Result {
        public final Exception exception;

        public Result(Exception exception) {
            this.exception = exception;
        }
    }
}
