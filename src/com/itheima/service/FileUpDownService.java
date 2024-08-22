package com.itheima.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件上传下载功能定义
 */
public interface FileUpDownService {
    /**
     * 上传
     * @param agreement 协议
     * @param netIn
     * @param netOut
     * @throws IOException
     */
    void uploadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException;

    /**
     * 下载
     * @param agreement
     * @param netIn
     * @param netOut
     * @throws IOException
     */
    void downloadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException;

    /**
     * 浏览
     * @param agreement
     * @param netIn
     * @param netOut
     * @throws IOException
     */
    void scanDirectory(String agreement, InputStream netIn, OutputStream netOut) throws IOException;
}
