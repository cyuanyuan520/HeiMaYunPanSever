package com.itheima.service;

import com.itheima.exception.BusinessException;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class YunPanServiceImp implements YunPanService{
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    public YunPanServiceImp() {
        init();//创建YunPanServiceImp对象的时候就进行好初始化
    }

    @Override
    public void init() {
        //初始化服务器端口
        ResourceBundle bundle = ResourceBundle.getBundle("yunpan");
        int port = Integer.parseInt(bundle.getString("serverPort"));
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[Notice]:端口创建成功");
        } catch (IOException e) {
            throw new BusinessException("创建端口失败，检查端口是否有冲突");
        }
        //初始化线程池
        threadPool = Executors.newFixedThreadPool(50);
    }

    @Override
    public void start() {
        //接收客户端连接 使用线程池统一管理
        while (true){
            try {
                System.out.println("[Notice]:等待连接...");
                Socket socket = serverSocket.accept();
                System.out.println("[Notice]:检测到新设备链接...");
                threadPool.submit(new FileUpDownServiceImp(socket));
            } catch (IOException e) {
                throw new BusinessException(e.getMessage());
            }
        }
    }
}
