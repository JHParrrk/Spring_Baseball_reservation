package com.firstspring.reservation.match.repository;

import com.firstspring.reservation.match.entity.MatchInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * [스프링 입문] MatchInfo 엔티티에 대한 JPA 레포지터리입니다.
 *
 * JpaRepository<MatchInfo, Long>를 상속하는 것만으로
 * save(), findById(), findAll(), deleteById() 등 기본 CRUD 메서드를 무료로 제공받습니다.
 *
 * 추가적인 쿼리가 필요하면 메서드 이름 규칙 또는 @Query 로 정의할 수 있습니다.
 */
public interface MatchRepository extends JpaRepository<MatchInfo, Long> {
    List<MatchInfo> findByStatusIn(List<MatchInfo.MatchStatus> statuses);

    Page<MatchInfo> findByStatusIn(List<MatchInfo.MatchStatus> statuses, Pageable pageable);
}
