package com.example.prompter.android;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileLoader extends AsyncTaskLoader<FileLoader.Result> {
    private static final String TAG = "FileLoader";
    private final File file;

    public FileLoader(Context context, File file) {
        super(context);

        this.file = file;
    }

    @Override
    public Result loadInBackground() {
        Log.d(TAG, "loadInBackground");
        StringBuilder sb = new StringBuilder();
        Exception exception = null;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line)
                        .append('\n');
            }
        } catch (IOException e) {
            exception = e;
        }

        return new Result(sb.toString(), exception);
    }

    public static class Result {
        public final String data;
        public final Exception exception;

        public Result(String data, Exception exception) {
            this.data = data;
            this.exception = exception;
        }
    }
}
