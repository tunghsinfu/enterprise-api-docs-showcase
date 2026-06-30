package com.example.demorestdocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class DemoRestDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoRestDocsApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer staticDocsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/docs/**")
                        .addResourceLocations("file:./docs/");
            }
        };
    }
}
