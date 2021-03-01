package com.linglong.rpc.test.protocol;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test implements BeanMessage {
    private String id;
    private Map<String, Long> map = new HashMap<>();
    private List<String> list = new ArrayList<>();

    private int code;
    private String msg;

    public Test() {
    }

    public Test(String id, Map<String, Long> map, List<String> list) {
        this.id = id;
        this.map = map;
        this.list = list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Long> getMap() {
        return map;
    }

    public void setMap(Map<String, Long> map) {
        this.map = map;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

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
}
