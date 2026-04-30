package com.firstspring.reservation.match.controller;

import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.match.dto.MatchResponse;
import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.repository.MatchRepository;
import com.firstspring.reservation.seat.dto.SeatResponse;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@Tag(name = "Match API", description = "경기 목록 및 좌석 조회 API")
public class MatchController {

    private static final List<MatchInfo.MatchStatus> VISIBLE_STATUSES = List.of(MatchInfo.MatchStatus.UPCOMING,
            MatchInfo.MatchStatus.ON_SALE);

    private final MatchRepository matchRepository;
    private final SeatRepository seatRepository;

    public MatchController(MatchRepository matchRepository, SeatRepository seatRepository) {
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping
    @Operation(summary = "경기 목록 조회", description = "예매 가능하거나 예정된 경기 목록을 조회합니다. (UPCOMING, ON_SALE만 포함. CANCELLED/CLOSED 제외)")
    public ResponseEntity<Page<MatchResponse>> getAllMatches(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MatchResponse> matches = matchRepository.findByStatusIn(VISIBLE_STATUSES, pageable)
                .map(MatchResponse::from);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "경기 잔여 좌석 조회", description = "특정 경기의 예매 가능한(AVAILABLE) 좌석 목록을 조회합니다.")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long id) {
        matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("경기를 찾을 수 없습니다. ID: " + id));
        List<SeatResponse> seats = seatRepository
                .findByMatchIdAndStatus(id, Seat.Status.AVAILABLE).stream()
                .map(SeatResponse::from)
                .toList();
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}/seats/all")
    @Operation(summary = "경기 전체 좌석 조회", description = "특정 경기의 모든 좌석과 예매 상태를 조회합니다.")
    public ResponseEntity<List<SeatResponse>> getAllSeats(@PathVariable Long id) {
        matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("경기를 찾을 수 없습니다. ID: " + id));
        List<SeatResponse> seats = seatRepository.findByMatchId(id).stream()
                .map(SeatResponse::from)
                .toList();
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}")
    @Operation(summary = "경기 단건 조회", description = "특정 경기의 상세 정보를 조회합니다.")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id) {
        MatchResponse match = matchRepository.findById(id)
                .map(MatchResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("경기를 찾을 수 없습니다. ID: " + id));
        return ResponseEntity.ok(match);
    }
}
