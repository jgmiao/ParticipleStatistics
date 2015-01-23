package com.secneo.participle.util;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Config {
    public static final Config i = new Config();

    private Config() {
        PropertiesConfiguration c = null;
        try {
            c = new PropertiesConfiguration("apkname.conf");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.dict_file_dir = c.getString("dict_file_dir");
    }

    public final String dict_file_dir;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
