package com.ctgu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// 启动时加载类
@Configuration
// 启用Swagger API文档
@EnableSwagger2
public class SwaggerConfig
{
	@Bean
	public Docket api()
	{
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.ctgu"))
				.paths(PathSelectors.any())
				.build();
	}

	private ApiInfo apiInfo()
	{
		return new ApiInfoBuilder().title("activiti6WithSpringboot")
				.description("api文档")
				.version("1.0")
				.contact(new Contact("lh2", "https://cn.bing.com", "2110931055@qq.com"))
				.build();
	}
}
