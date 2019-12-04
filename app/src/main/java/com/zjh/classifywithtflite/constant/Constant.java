package com.zjh.classifywithtflite.constant;

public class Constant {

    // 服务器IP地址
    private final static String SERVER_IP = "10.174.162.71";
    // 服务器上的项目名称
    private final static String SERVER_PROJECT_NAME = "ImageClassificationServer_war_exploded";
    // 拼接形成请求地址
    public final static String REQUEST_URL = "http://" + SERVER_IP + ":8080/" + SERVER_PROJECT_NAME + "/";

    // 各个Servlet的请求地址
    public final static String ADMIN_LOGIN_URL      = REQUEST_URL + "admin/login";
    public final static String USER_LOGIN_URL       = REQUEST_URL + "user/login";
    public final static String USER_REGISTER_URL    = REQUEST_URL + "user/register";
    public final static String VIEW_LABEL_URL       = REQUEST_URL + "label/viewLabels";
    public final static String ADD_LABEL_URL        = REQUEST_URL + "label/addLabel";
    public final static String DELETE_LABEL_URL     = REQUEST_URL + "label/deleteLabel";
    public final static String VIEW_IMAGES_URL      = REQUEST_URL + "image/viewImages";
    public final static String ADD_IMAGE_URL        = REQUEST_URL + "image/addImage";
    public final static String DELETE_IMAGE_URL     = REQUEST_URL + "image/deleteImage";
}
