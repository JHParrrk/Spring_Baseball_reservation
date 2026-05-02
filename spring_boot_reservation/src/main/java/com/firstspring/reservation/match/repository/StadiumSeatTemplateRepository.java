package com.firstspring.reservation.match.repository;

import com.firstspring.reservation.match.entity.StadiumSeatTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StadiumSeatTemplateRepository extends JpaRepository<StadiumSeatTemplate, Long> {
    List<StadiumSeatTemplate> findByStadiumNameOrderBySeatNumberAsc(String stadiumName);

    List<StadiumSeatTemplate> findAllByOrderByStadiumNameAscSeatNumberAsc();

    boolean existsByStadiumName(String stadiumName);
}
