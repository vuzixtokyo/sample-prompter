package com.example.prompter.android.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    private static final String DIR_NAME = "prompter";
    private static final File EXTERNAL_SDCARD_DIR = new File("/mnt/ext_sdcard");

    public static File getDir(Context context) {
        File dir = new File(EXTERNAL_SDCARD_DIR, DIR_NAME);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File writableTest = new File(dir, "");
        try {
            writableTest.createNewFile();
            writableTest.deleteOnExit();
        } catch (IOException e) {
            dir = context.getDir("", Context.MODE_PRIVATE);
        }

        return dir;
    }
}
