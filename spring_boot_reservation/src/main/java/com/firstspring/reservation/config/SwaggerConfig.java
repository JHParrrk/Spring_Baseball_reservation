package com.firstspring.reservation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [스프링 입문] Swagger(OpenAPI) UI 설정 클래스입니다.
 *
 * @Configuration : 이 클래스가 스프링 설정(Bean 정의)을 담당함을 선언합니다.
 *                → 스프링 컨테이너가 시작될 때 이 클래스를 읽어 @Bean 메서드들을 실행합니다.
 *
 *                Swagger UI는 /swagger-ui/index.html 에서 확인할 수 있습니다.
 *                이 설정을 통해 API 테스트 화면에서 JWT Bearer 토큰 인증을 사용할 수 있습니다.
 */
@Configuration
public class SwaggerConfig {

        /**
         * [스프링 입문] @Bean 메서드
         * 반환된 OpenAPI 객체를 스프링 컨테이너에 Bean으로 등록합니다.
         * Swagger 라이브러리(springdoc-openapi)가 이 Bean을 읽어 UI를 구성합니다.
         *
         * Bearer 인증 설정을 추가해서 Swagger 화면의 "Authorize" 버튼으로
         * JWT 토큰을 입력하면 이후 모든 API 호출에 자동으로 Authorization 헤더가 추가됩니다.
         */
        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("프로야구 구장 예매 시스템 API (Baseball Reservation System)")
                                                .description("MSA 아키텍처 기반의 프로야구 구장(1천 석) 예매 시스템을 위한 API 명세서입니다.\n\n" +
                                                                "**인증 방법:** [Google 로그인](/login) 후 발급받은 JWT 토큰을 우측 상단 `Authorize` 버튼에 입력하세요.\n\n"
                                                                +
                                                                "- `Reservation API`: 예약 정보 생성, 수정, 삭제 관련 기능\n" +
                                                                "- `Infra Test API`: 인프라 구성요소(DB, Kafka, Redis) 연동 여부 테스트")
                                                .version("1.0.0"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사 없이 토큰만 입력하면 됩니다.")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}
