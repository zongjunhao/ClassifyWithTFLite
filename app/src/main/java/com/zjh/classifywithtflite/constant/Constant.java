package com.zjh.classifywithtflite.constant;

public class Constant {

    // 服务器IP地址
    private final static String SERVER_IP = "10.174.136.193";
    // 服务器上的项目名称
    private final static String SERVER_PROJECT_NAME = "ImageClassificationServer_war";
    // 拼接形成请求地址
    private final static String REQUEST_URL = "http://" + SERVER_IP + "/" + SERVER_PROJECT_NAME + "/";

    // 各个Servlet的请求地址
    public final static String ADMIN_LOGIN_URL = REQUEST_URL + "admin/login";
}
