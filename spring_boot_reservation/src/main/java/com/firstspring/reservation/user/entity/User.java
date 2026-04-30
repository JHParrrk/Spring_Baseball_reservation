package com.firstspring.reservation.user.entity;

import jakarta.persistence.*;

/**
 * [스프링 입문] 회원(User) 엔티티입니다.
 *
 * @Entity : 이 클래스가 DB 테이블과 매핑되는 JPA 엔티티임을 선언합니다.
 *         → 하이버네이트가 이 클래스를 읽어 SQL 쿼리를 자동 생성합니다.
 * @Table(name = "users") : 매핑할 DB 테이블 이름을 지정합니다. (user는 SQL 예약어라 실제로는 users를
 *             사용)
 *
 *             JPA 엔티티 중요 규칙:
 *             1) 이 클래스에 등록된 필드 = DB 컴럼
 *             2) @Id = Primary Key
 *             3) @GeneratedValue = DB가 자동 증가시켜줌 (AUTO_INCREMENT)
 *             4) @Column = 컴럼 속성 설정 (nullable, unique, length 등)
 */
@Entity
@Table(name = "users")
public class User {

    // @Id : 이 필드가 PK(프라이머리 키)임을 선언합니다
    @Id
    // GenerationType.IDENTITY : DB의 AUTO_INCREMENT를 사용합니다
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String provider; // 구글 등 소셜로그인 제공자

    private String providerId; // 구글 고유 ID

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'active'")
    private String status = "active";

    public User() {
    }

    // 기본 가입용
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // 소셜 로그인 가입용
    public User(String name, String email, String provider, String providerId, String roleStr) {
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        // DB 호환성을 위해 순수 'USER' 또는 'ADMIN' 저장
        this.role = "ADMIN".equals(roleStr) || "ROLE_ADMIN".equals(roleStr) ? "ADMIN" : "USER";
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    private static final java.util.Set<String> ALLOWED_STATUSES = java.util.Set.of("active", "inactive", "suspended",
            "blacklisted");

    public void setStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("허용되지 않는 상태값입니다: " + status);
        }
        this.status = status;
    }

    public void setRole(String role) {
        this.role = "ADMIN".equals(role) || "ROLE_ADMIN".equals(role) ? "ADMIN" : "USER";
    }

    public User updateName(String name) {
        this.name = name;
        return this;
    }
}