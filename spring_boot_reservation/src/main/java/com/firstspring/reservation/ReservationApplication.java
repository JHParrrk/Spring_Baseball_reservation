package com.firstspring.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [스프링 입문] 애플리케이션의 시작점(Entry Point)입니다.
 *
 * @SpringBootApplication 은 세 가지 어노테이션을 한꺼번에 포함합니다:
 *                        - @Configuration : 이 클래스가 Bean 설정 클래스임을 선언
 *                        - @EnableAutoConfiguration : spring.factories 기반 자동 설정
 *                        활성화
 *                        - @ComponentScan : 현재 패키지
 *                        하위의 @Component, @Service, @Repository, @Controller 를
 *                        자동으로 스캔해 Bean으로 등록
 *
 *                        SpringApplication.run() 을 호출하면 내장 Tomcat 서버가 시작되고
 *                        IoC 컨테이너(ApplicationContext)가 구성됩니다.
 */
@EnableJpaAuditing
@SpringBootApplication
public class ReservationApplication {

	public static void main(String[] args) {
		// 스프링 부트 애플리케이션을 실행하는 단 한 줄의 코드
		SpringApplication.run(ReservationApplication.class, args);
	}

}
