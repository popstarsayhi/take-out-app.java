package com.reggie.config;

import com.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射
     * @param registry
     *
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("start static resource mapping");
        //access path
        registry.addResourceHandler("/backend/**")
                .addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**")
                .addResourceLocations("classpath:/front/");
    }

    /**
     * extend MVC message converter
     *
     * @param converters
     */

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        log.info("message converter ");

        //create message converter
        MappingJackson2HttpMessageConverter messageConveter = new MappingJackson2HttpMessageConverter();
        //convert java object to jason via jackson
        messageConveter.setObjectMapper(new JacksonObjectMapper());
        //add to MVC message converter
        converters.add(0, messageConveter);

        super.extendMessageConverters(converters);
    }
}
