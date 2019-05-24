package com.sq.tools.utils;


import org.json.JSONObject;

public class ResponseModel {

    public static final int SUCCESS = 0;

    //正在处理中
    public static final int COMMON_PROCESSING = -995;

    public static final int COMMON_SYSTEM_EXCEPTION = -996;

    public static final int COMMON_PARAMS_NULL = -997;

    public static final int COMMON_PARAMS_INVALID = -998;

    public ResponseModel(int code){
        this.code = code;
    }

    public ResponseModel(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public ResponseModel(JSONObject jo){
        this.code = SUCCESS;
        this.jsonData = jo.toString();
    }

    public ResponseModel(String joStr){
        this.code = SUCCESS;
        this.jsonData = joStr;
    }

    private int code;

    private String msg;

    private String jsonData;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", jsonData='" + jsonData + '\'' +
                '}';
    }
}
