package com.firstspring.reservation.match.controller;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.match.dto.MatchCreateRequest;
import com.firstspring.reservation.match.dto.MatchResponse;
import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.repository.MatchRepository;
import com.firstspring.reservation.seat.dto.SeatBulkCreateRequest;
import com.firstspring.reservation.seat.dto.SeatResponse;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/matches")
@Tag(name = "Admin - Match API", description = "경기 등록/상태 변경/좌석 일괄 생성 (ADMIN 전용)")
public class AdminMatchController {

    /**
     * 허용되는 경기 상태 전이 규칙 (State Machine).
     * CLOSED, CANCELLED는 단말 상태로 전이 불가능합니다.
     */
    private static final Map<MatchInfo.MatchStatus, Set<MatchInfo.MatchStatus>> VALID_TRANSITIONS = Map.of(
            MatchInfo.MatchStatus.UPCOMING, Set.of(MatchInfo.MatchStatus.ON_SALE, MatchInfo.MatchStatus.CANCELLED),
            MatchInfo.MatchStatus.ON_SALE, Set.of(MatchInfo.MatchStatus.CLOSED, MatchInfo.MatchStatus.CANCELLED),
            MatchInfo.MatchStatus.CLOSED, Set.of(),
            MatchInfo.MatchStatus.CANCELLED, Set.of());

    private final MatchRepository matchRepository;
    private final SeatRepository seatRepository;

    public AdminMatchController(MatchRepository matchRepository, SeatRepository seatRepository) {
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
    }

    @PostMapping
    @Operation(summary = "경기 등록", description = "신규 경기를 등록합니다. 기본 상태는 UPCOMING(예매 오픈 전)입니다.")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchCreateRequest request) {
        MatchInfo match = new MatchInfo(request.title(), request.matchDate(), request.stadiumName());
        MatchInfo saved = matchRepository.save(match);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(MatchResponse.from(saved));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Operation(summary = "경기 상태 변경", description = "경기 상태를 변경합니다. 허용 전이: UPCOMING → ON_SALE/CANCELLED, ON_SALE → CLOSED/CANCELLED")
    public ResponseEntity<MatchResponse> updateMatchStatus(
            @PathVariable Long id,
            @RequestParam MatchInfo.MatchStatus status) {
        MatchInfo match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("경기를 찾을 수 없습니다. ID: " + id));
        Set<MatchInfo.MatchStatus> allowed = VALID_TRANSITIONS.getOrDefault(match.getStatus(), Set.of());
        if (!allowed.contains(status)) {
            throw new InvalidRequestException(String.format(
                    "경기 상태 전환이 불가능합니다: %s → %s (허용: %s)", match.getStatus(), status, allowed));
        }
        match.setStatus(status);
        return ResponseEntity.ok(MatchResponse.from(matchRepository.save(match)));
    }

    @PostMapping("/{id}/seats")
    @Transactional
    @Operation(summary = "좌석 일괄 생성", description = "특정 경기에 좌석을 일괄 등록합니다. CANCELLED/CLOSED 경기에는 추가할 수 없습니다.")
    public ResponseEntity<List<SeatResponse>> createSeats(
            @PathVariable Long id,
            @Valid @RequestBody SeatBulkCreateRequest request) {
        MatchInfo match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("경기를 찾을 수 없습니다. ID: " + id));
        if (match.getStatus() == MatchInfo.MatchStatus.CANCELLED
                || match.getStatus() == MatchInfo.MatchStatus.CLOSED) {
            throw new InvalidRequestException(
                    "종료되거나 취소된 경기에는 좌석을 추가할 수 없습니다. 현재 상태: " + match.getStatus());
        }
        List<String> requestedNumbers = request.seats().stream()
                .map(SeatBulkCreateRequest.SeatItem::seatNumber)
                .toList();
        Set<String> existing = seatRepository.findByMatchIdAndSeatNumberIn(id, requestedNumbers)
                .stream().map(Seat::getSeatNumber).collect(Collectors.toSet());
        if (!existing.isEmpty()) {
            throw new InvalidRequestException("이미 등록된 좌석 번호가 있습니다: " + existing);
        }
        List<Seat> seats = request.seats().stream()
                .map(item -> new Seat(match, item.seatNumber(), item.tier(), item.price()))
                .toList();
        List<SeatResponse> responses = seatRepository.saveAll(seats).stream()
                .map(SeatResponse::from)
                .toList();
        return ResponseEntity.status(201).body(responses);
    }
}
