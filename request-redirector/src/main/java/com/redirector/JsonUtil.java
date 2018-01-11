package com.redirector;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;

@Component
public class JsonUtil {

    @Autowired
    private ObjectMapper mapper;

    public static <T> T fromJson(String json) {
        return instance.fromJsonInternal(json);
    }

    /**
     *
     *   TestObject[] list3 = JsonUtil.fromJson(json, TestObject[].class);
     *      returns TestObject[]
     *   List<TestObject> list0 = JsonUtil.fromJson(json);
     *      returns List<LinkedHshMap<String, Object>> ....
     *   List<TestObject> list1 = JsonUtil.fromJson(json, new TypeReference<List<TestObject>>() {});
     *      returns List<TestObject> ....
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return instance.fromJsonInternal(json, clazz);
    }

    /**
     * Map<String, ProxyConfig> proxyConfig = JsonUtil.fromJson(serverDto.getProxy(),
     *                                                          new TypeReference<Map<String, ProxyConfig>>() {})
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, final TypeReference<T> type) {
        return instance.fromJsonInternal(json, type);
    }

    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        return instance.fromJsonToListInternal(json, clazz);
    }

    public static String toJson(Object obj) {
        return instance.toJsonInternal(obj);
    }

    private static JsonUtil instance;
    @PostConstruct
    public void init() {
        instance = this;
    }

    private <T> T fromJsonInternal(String json) {
        if(isNull(json)) return null;

        try {
            return mapper.readValue(json, new TypeReference<T>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> T fromJsonInternal(String json, Class<T> clazz) {
        if(isNull(json)) return null;

        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> T fromJsonInternal(String json, final TypeReference<T> type) {
        if(isNull(json)) return null;

        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> List<T> fromJsonToListInternal(String json, Class<T> clazz) {
        if(isNull(json)) return Lists.newArrayList();

        try {
            return mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String toJsonInternal(Object obj) {
        if(isNull(obj)) return null;

        String json = "";
        try {
            json = mapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

}
