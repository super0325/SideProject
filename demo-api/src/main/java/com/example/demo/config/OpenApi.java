package com.example.demo.config;

import java.util.Arrays;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApi {
	
	@Value("${openApiUrl}")
	private String openApiUrl;
	@Value("${openApiServer}")
	private String openApiServer;

    @Bean
    OpenAPI customOpenAPI() {
    	
        // 建立並配置 OpenAPI 實例
        return new OpenAPI()
        		// 設定 API 資訊
        		.info(new Info()
        				.title("我的應用程式 API") // 設定標題
        				.version("v1") // 設定版本
        				.description("這是一個使用 springdoc-openapi 和 OpenAPI 3 的範例 Spring Boot RESTful 服務。") // 設定描述資訊
        				.contact(new Contact()
        						.name("Andy Huang") // 設定聯絡人姓名
        						.email("andy.huang@example.com") // 設定聯絡人電子郵件
        						.url("http://example.com")) // 設定聯絡人網址
        				.license(new License()
        						.name("Apache 2.0") // 設定許可證名稱
        						.url("http://springdoc.org"))) // 設定許可證 URL
                // 新增安全方案
                .components(new Components()
                        .addSecuritySchemes("basicScheme", // 新增名稱為 "basicScheme" 的安全方案
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // 指定方案類型為 HTTP
                                        .scheme("basic"))) // 使用基本認證方式
                // 新增安全要求
                .addSecurityItem(new SecurityRequirement().addList("basicScheme")) // 新增基本認證方案的安全要求
		        .servers(Arrays.asList(
	        	    new Server().url(openApiUrl).description(openApiServer)
	        	));
    }
    
    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
    
}
