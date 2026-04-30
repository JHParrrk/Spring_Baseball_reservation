package com.firstspring.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [MSA] Payment 서비스 Swagger(OpenAPI) 설정 클래스입니다.
 *
 * Swagger UI는 http://localhost:8081/swagger-ui/index.html 에서 확인할 수 있습니다.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("결제 서비스 API (Payment System)")
                        .description("MSA 아키텍처 기반의 프로야구 예매 시스템 - 결제 모듈 API 명세서입니다.\n\n" +
                                "- `Payment API`: 결제 상태 조회 및 이력 관련 기능 (현재 Mock 결제 시뮬레이션 중)")
                        .version("1.0.0"));
    }
}
