package com.hjs.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.security.krb5.internal.rcache.DflCache;

import java.util.Properties;

/**
 * @author hong
 * @create 2023-01-02 15:30
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer(){
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();

        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","77,88,94");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789qwertyuiopasdfghjklzxcvbnm");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

}
