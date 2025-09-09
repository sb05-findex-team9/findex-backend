package com.codeit.findex.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("FIndex API 문서")
				.description("FIndex 프로젝트의 Swagger API 문서입니다.")
				.version("v1.0.0")
				.contact(new Contact()
					.name("개발팀")
					.email("halogiju123@gmail.com")
					.url("https://github.com/sb05-findex-team9"))
				.license(new License()
					.name("MIT License")
					.url("https://opensource.org/licenses/MIT")))
			.servers(List.of(
				new Server().url("http://localhost:8080").description("로컬 개발 서버"),
				new Server().url("배포하면 넣어야해~~").description("프로덕션 서버")
			));
	}
}