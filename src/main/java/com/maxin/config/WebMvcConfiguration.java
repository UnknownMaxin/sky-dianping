package com.maxin.config;

import com.maxin.interceptor.LoginInterceptor;
import com.maxin.interceptor.RefreshTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(
//                "/user/code", "/user/login", "/blog/hot", "/shop/**", "/shop-type/**", "/upload/**", "/voucher/**",
//                "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
//                "/swagger-ui/index.html", "/favicon.ico", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js"
//                ).order(1);
        registry.addInterceptor(new RefreshTokenInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
    }

    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 资源映射
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.15.5/");
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // API 文档资源映射
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

//    @Bean
//    public Docket createApiInfo() {
//        ApiInfo apiInfo = new ApiInfoBuilder().title("小众点评项目接口文档").build();
//        Docket docket = new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.maxin.controller"))
//                .paths(PathSelectors.any())
//                .build();
//        return docket;
//    }
}
