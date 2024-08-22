package com.itheima.service;

import com.itheima.exception.BusinessException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 云盘服务端
 */
public class YunPanServiceImp implements YunPanService {
    private ServerSocket serverSocket;//服务端对象
    private ResourceBundle bundle;//资源读
    private ExecutorService threadPool;//线程池

    public YunPanServiceImp() {
        //创建对象的时候就进行初始化
        init();
    }

    /**
     * 1.读取配置文件中端口信息，初始化服务端
     * 2.线程池初始化
     */
    @Override
    public void init() {
        //读取配置文件中端口信息，初始化服务端
        bundle = ResourceBundle.getBundle("yunpan");
        // 8888
        int port = Integer.parseInt(bundle.getString("serverPort"));
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new BusinessException("创建端口失败，检查端口是否有冲突");
        }
        // 线程池初始化
        threadPool = Executors.newFixedThreadPool(50);
    }

    /**
     * 接收客户端连接,使用线程池统一处理
     */
    @Override
    public void start() {
        //接收客户端连接,使用线程池统一处理
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("检测到设备链接!");
                //接收到请求后，业务由线程池进行处理
                threadPool.submit(new FileUpDownServiceImp(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
