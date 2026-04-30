package com.firstspring.reservation.user.repository;

import com.firstspring.reservation.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [스프링 입문] User 엔티티에 대한 JPA 레포지터리입니다.
 *
 * JpaRepository<User, Long>를 상속으로 확장하면
 * 다음 메서드들을 무료로 제공받습니다:
 * - save(), findById(), findAll(), deleteById() 등
 *
 * 메서드 이름 규칙에 따라 자동으로 SQL이 생성됩니다:
 * - findByEmail(email) -> SELECT * FROM users WHERE email = ?
 *
 * 인터페이스만 선언하면 구현 코드는 하이버네이트가 대신 만들어줍니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional<User> : 유저가 없을 수도 있으므로 NullPointerException 별로 체크 없이 안전하게 다뢰기 위해
    // Optional로 반환
    Optional<User> findByEmail(String email);
}