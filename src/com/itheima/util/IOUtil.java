package com.itheima.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 工具类，IO流拷贝
 */
public class IOUtil {
    public static void copy(InputStream in, OutputStream out) throws IOException {
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) != -1) {
            // System.out.println("read len=" + len);
            out.write(buf, 0, len);
        }
    }
}
