package com.firstspring.reservation.match.controller;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.match.dto.MatchCreateRequest;
import com.firstspring.reservation.match.dto.MatchResponse;
import com.firstspring.reservation.match.dto.MatchScheduleLogResponse;
import com.firstspring.reservation.match.dto.StadiumTemplateCreateRequest;
import com.firstspring.reservation.match.dto.StadiumTemplateDetailResponse;
import com.firstspring.reservation.match.dto.StadiumTemplateSummaryResponse;
import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.entity.StadiumSeatTemplate;
import com.firstspring.reservation.match.repository.MatchRepository;
import com.firstspring.reservation.match.repository.StadiumSeatTemplateRepository;
import com.firstspring.reservation.match.service.MatchScheduleLogStore;
import com.firstspring.reservation.seat.dto.SeatBulkCreateRequest;
import com.firstspring.reservation.seat.dto.SeatResponse;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/matches")
@Tag(name = "Admin - Match API", description = "경기/구장 템플릿 등록 및 상태 변경 (ADMIN 전용)")
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
    private final StadiumSeatTemplateRepository stadiumSeatTemplateRepository;
    private final MatchScheduleLogStore scheduleLogStore;

    public AdminMatchController(
            MatchRepository matchRepository,
            SeatRepository seatRepository,
            StadiumSeatTemplateRepository stadiumSeatTemplateRepository,
            MatchScheduleLogStore scheduleLogStore) {
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
        this.stadiumSeatTemplateRepository = stadiumSeatTemplateRepository;
        this.scheduleLogStore = scheduleLogStore;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "경기 목록 조회 (관리자)", description = "모든 경기 상태(UPCOMING/ON_SALE/CLOSED/CANCELLED)를 페이지로 조회합니다.")
    public ResponseEntity<Page<MatchResponse>> getAllMatchesForAdmin(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MatchResponse> matches = matchRepository.findAll(pageable)
                .map(MatchResponse::from);
        return ResponseEntity.ok(matches);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "경기 등록", description = "신규 경기를 등록합니다. 선택한 구장 템플릿의 좌석이 자동 생성됩니다.")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchCreateRequest request) {
        List<StadiumSeatTemplate> templates = stadiumSeatTemplateRepository
                .findByStadiumNameOrderBySeatNumberAsc(request.stadiumName());
        if (templates.isEmpty()) {
            throw new InvalidRequestException(
                    "선택한 구장의 좌석 템플릿이 없습니다. 먼저 구장 좌석 템플릿을 등록하세요: " + request.stadiumName());
        }

        MatchInfo match = new MatchInfo(request.title(), request.matchDate(), request.stadiumName());
        MatchInfo saved = matchRepository.saveAndFlush(match);

        List<Seat> seats = templates.stream()
                .map(t -> new Seat(saved, t.getSeatNumber(), t.getTier(), t.getPrice()))
                .toList();
        seatRepository.saveAll(seats);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(MatchResponse.from(saved));
    }

    @GetMapping("/stadium-templates")
    @Transactional(readOnly = true)
    @Operation(summary = "구장 좌석 템플릿 목록 조회", description = "등록된 구장별 좌석 템플릿 개수를 조회합니다.")
    public ResponseEntity<List<StadiumTemplateSummaryResponse>> getStadiumTemplates() {
        List<StadiumSeatTemplate> all = stadiumSeatTemplateRepository.findAllByOrderByStadiumNameAscSeatNumberAsc();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (StadiumSeatTemplate seat : all) {
            counts.put(seat.getStadiumName(), counts.getOrDefault(seat.getStadiumName(), 0) + 1);
        }

        List<StadiumTemplateSummaryResponse> result = counts.entrySet().stream()
                .map(e -> new StadiumTemplateSummaryResponse(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stadium-templates/{stadiumName}")
    @Transactional(readOnly = true)
    @Operation(summary = "구장 좌석 템플릿 상세 조회", description = "선택한 구장의 좌석 번호/등급/가격 목록을 조회합니다.")
    public ResponseEntity<StadiumTemplateDetailResponse> getStadiumTemplateDetail(
            @PathVariable String stadiumName) {
        List<StadiumSeatTemplate> seats = stadiumSeatTemplateRepository.findByStadiumNameOrderBySeatNumberAsc(stadiumName);
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("구장 좌석 템플릿을 찾을 수 없습니다: " + stadiumName);
        }
        return ResponseEntity.ok(StadiumTemplateDetailResponse.from(stadiumName, seats));
    }

    @PostMapping("/stadium-templates")
    @Transactional
    @Operation(summary = "구장 좌석 템플릿 등록", description = "신규 구장의 좌석 템플릿(좌석번호/등급/가격)을 등록합니다.")
    public ResponseEntity<StadiumTemplateDetailResponse> createStadiumTemplate(
            @Valid @RequestBody StadiumTemplateCreateRequest request) {
        String stadiumName = request.stadiumName().trim();
        if (stadiumSeatTemplateRepository.existsByStadiumName(stadiumName)) {
            throw new InvalidRequestException("이미 등록된 구장입니다. 신규 구장만 등록할 수 있습니다: " + stadiumName);
        }

        Set<String> uniqueSeatNumbers = new HashSet<>();
        for (StadiumTemplateCreateRequest.SeatTemplateItem seat : request.seats()) {
            String seatNumber = seat.seatNumber().trim();
            if (!uniqueSeatNumbers.add(seatNumber)) {
                throw new InvalidRequestException("중복된 좌석 번호가 있습니다: " + seatNumber);
            }
        }

        List<StadiumSeatTemplate> templates = request.seats().stream()
                .map(item -> new StadiumSeatTemplate(
                        stadiumName,
                        item.seatNumber().trim(),
                        item.tier().trim(),
                        item.price()))
                .toList();
        List<StadiumSeatTemplate> saved = stadiumSeatTemplateRepository.saveAll(templates);
        return ResponseEntity.status(201).body(StadiumTemplateDetailResponse.from(stadiumName, saved));
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

    @GetMapping("/scheduler/logs")
    @Operation(summary = "경기 스케줄 실행 로그 조회", description = "자동 예매 오픈/마감 스케줄 실행 결과를 최근순으로 조회합니다.")
    public ResponseEntity<List<MatchScheduleLogResponse>> getScheduleLogs(
            @RequestParam(defaultValue = "30") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        List<MatchScheduleLogResponse> logs = scheduleLogStore.getRecent(safeLimit).stream()
                .map(MatchScheduleLogResponse::from)
                .toList();
        return ResponseEntity.ok(logs);
    }
}
