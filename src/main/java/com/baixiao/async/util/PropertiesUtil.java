package com.baixiao.async.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/8/6 14:21
 */
@Slf4j
public class PropertiesUtil {
    private static Properties properties;
    private static boolean isFileExist=false;
    static {
        String fileName="application.properties";
        URL url = PropertiesUtil.class.getClassLoader().getResource(fileName);
        if (url!=null){
            File file = new File(url.getFile());
            if (file.exists()){
                isFileExist=true;
                properties=new Properties();
                try {
                    properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
                }catch (IOException e){
                    log.error("application.properties is not exist");
                }
            }
        }
    }

    public static String getProperty(String key){
        if (!isFileExist){
            return null;
        }
        String value = properties.getProperty(key.trim());
        if (value==null||value.length()==0){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultValue){
        if (!isFileExist){
            return defaultValue;
        }
        String value = properties.getProperty(key.trim());
        if (value==null||value.length()==0){
            return defaultValue;
        }
        return value.trim();
    }
}
