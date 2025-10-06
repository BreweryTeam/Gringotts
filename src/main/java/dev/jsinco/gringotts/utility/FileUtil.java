package dev.jsinco.gringotts.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class FileUtil {


    public static String readInternalResource(String path) {
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<File> listInternalFiles(String path) {
        URL url = FileUtil.class.getResource(path);
        if (url == null) {
            return List.of();
        }
        File[] files = new File(url.getFile()).listFiles();
        if (files != null) {
            return List.of(files);
        }
        return List.of();
    }

    public static File getInternalFile(String path) {
        URL url = FileUtil.class.getResource(path);
        if (url == null) {
            return null;
        }
        return new File(url.getFile());
    }
}
