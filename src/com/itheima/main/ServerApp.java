package com.itheima.main;

import com.itheima.service.YunPanService;
import com.itheima.service.YunPanServiceImp;

public class ServerApp {
    public static void main(String[] args) {
        // 创建服务对象
        YunPanService yunpan = new YunPanServiceImp();
        // 开始云盘服务
        yunpan.start();
    }
}
