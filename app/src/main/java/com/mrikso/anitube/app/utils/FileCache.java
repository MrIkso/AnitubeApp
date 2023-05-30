package com.mrikso.anitube.app.utils;

import com.google.common.io.Files;
import com.mrikso.anitube.app.App;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileCache {
    public static void writePage(String content) throws IOException {
        File chachePage = new File(App.getApplication().getCacheDir().getAbsolutePath() + "/page.html");
        Files.write(content, chachePage, StandardCharsets.UTF_8);
    }

    public static String readPage() throws IOException {
        File chachePage = new File(App.getApplication().getCacheDir().getAbsolutePath() + "/page.html");
        return Files.toString(chachePage, StandardCharsets.UTF_8);
    }
}
