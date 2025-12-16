package org.yyubin.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI bookVoyageOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("BookVoyage API")
                .description("""
                        ## 소셜 기반 독서 리뷰 플랫폼 API

                        BookVoyage는 책 리뷰를 중심으로 한 소셜 플랫폼입니다.

                        ### 주요 기능
                        - 리뷰 작성/수정/삭제 (Markdown 지원)
                        - 댓글 및 대댓글 (트리 구조)
                        - 리액션(좋아요) 및 북마크
                        - 팔로우/언팔로우
                        - 실시간 알림 (Kafka 기반)
                        - 추천 시스템 (도서/리뷰)

                        ### 인증 방식
                        - OAuth2 + JWT 기반 인증
                        - Google 소셜 로그인 지원
                        - Bearer Token 방식

                        ### 아키텍처
                        - 멀티모듈 헥사고날 아키텍처
                        - MySQL (SoT) + Neo4j + Elasticsearch
                        - Redis 캐싱 & 세션
                        - Kafka 이벤트 스트리밍
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("BookVoyage Team")
                        .url("https://github.com/yyubin")
                        .email("contact@bookvoyage.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
//                new Server()
//                        .url("https://api.bookvoyage.com")
//                        .description("Production Server")
        );
    }


    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT 토큰을 입력하세요.

                                        1. `/api/auth/login` 또는 `/api/auth/oauth2/google`로 로그인
                                        2. 응답에서 받은 `accessToken` 값을 복사
                                        3. 우측 상단 'Authorize' 버튼 클릭
                                        4. 토큰 입력 (Bearer 접두어 제외)

                                        예시: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                                        """));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Authentication");
    }
}
