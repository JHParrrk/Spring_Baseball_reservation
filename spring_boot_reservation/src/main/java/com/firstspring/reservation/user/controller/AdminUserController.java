package com.firstspring.reservation.user.controller;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.reservation.dto.UserReservationStat;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.user.dto.UserResponse;
import com.firstspring.reservation.user.dto.UserSummaryResponse;
import com.firstspring.reservation.user.entity.User;
import com.firstspring.reservation.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin - User API", description = "유저 상태/권한 관리 (ADMIN 전용)")
@PreAuthorize("hasRole('ADMIN')") // [C2] URL 레벨 보안에 추가하는 메서드 레벨 이중 방어
public class AdminUserController {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public AdminUserController(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "유저 목록 조회", description = "전체 유저 목록과 예매/취소 통계를 조회합니다.")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<Long> userIds = userPage.getContent().stream().map(User::getId).toList();

        // 유저별 예약 통계를 단 1번의 GROUP BY 쿼리로 로드 (N+1 방지)
        Map<Long, Map<Reservation.Status, Long>> statsMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            reservationRepository.countGroupByUserAndStatus(userIds).forEach(stat -> {
                statsMap.computeIfAbsent(stat.userId(), k -> new HashMap<>()).put(stat.status(), stat.count());
            });
        }

        Page<UserSummaryResponse> result = userPage.map(u -> {
            Map<Reservation.Status, Long> stats = statsMap.getOrDefault(u.getId(), Map.of());
            long total = stats.getOrDefault(Reservation.Status.CONFIRMED, 0L)
                    + stats.getOrDefault(Reservation.Status.PENDING, 0L);
            long cancelled = stats.getOrDefault(Reservation.Status.CANCELLED, 0L);
            return new UserSummaryResponse(u.getId(), u.getName(), u.getEmail(),
                    u.getRole(), u.getStatus(), total, cancelled);
        });
        return ResponseEntity.ok(result);
    }

    private static final Set<String> ALLOWED_STATUSES = Set.of("active", "inactive", "suspended", "blacklisted");
    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    @PatchMapping("/{id}/status")
    @Transactional
    @Operation(summary = "유저 상태 변경", description = "유저 상태를 변경합니다. (active / inactive / suspended / blacklisted)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        // [C3] 허용 값 이외의 임의 문자열 삽입 방지
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new InvalidRequestException("허용되지 않는 status 값입니다. 허용: " + ALLOWED_STATUSES);
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다. ID: " + id));
        user.setStatus(status);
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }

    @PatchMapping("/{id}/role")
    @Transactional
    @Operation(summary = "유저 권한 변경", description = "유저 권한을 변경합니다. (USER / ADMIN)")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        // [C3] 허용 값 이외의 임의 문자열 삽입 방지
        if (!ALLOWED_ROLES.contains(role)) {
            throw new InvalidRequestException("허용되지 않는 role 값입니다. 허용: " + ALLOWED_ROLES);
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다. ID: " + id));
        user.setRole(role);
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }
}
