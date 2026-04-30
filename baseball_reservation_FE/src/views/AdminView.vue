<template>
  <div class="admin-page">
    <h1 class="page-title">관리자 페이지</h1>

    <!-- 탭 네비게이션 -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- ===== 경기 관리 탭 ===== -->
    <section v-if="activeTab === 'matches'" class="tab-content">
      <div class="card">
        <h2>경기 등록</h2>
        <form class="form-grid" @submit.prevent="createMatch">
          <label
            >제목
            <input v-model="matchForm.title" placeholder="경기 제목" required />
          </label>
          <label
            >날짜/시간
            <input
              v-model="matchForm.matchDate"
              type="datetime-local"
              required
            />
          </label>
          <label
            >경기장
            <input
              v-model="matchForm.stadiumName"
              placeholder="경기장 이름"
              required
            />
          </label>
          <button type="submit" class="btn-primary" :disabled="matchLoading">
            등록
          </button>
        </form>
        <p v-if="matchMsg" :class="matchMsgError ? 'msg-error' : 'msg-ok'">
          {{ matchMsg }}
        </p>
      </div>

      <div class="card">
        <h2>경기 상태 변경</h2>
        <div class="row-form">
          <input
            v-model.number="statusMatchId"
            type="number"
            placeholder="경기 ID"
          />
          <select v-model="statusMatchValue">
            <option value="UPCOMING">UPCOMING</option>
            <option value="ON_SALE">ON_SALE</option>
            <option value="CLOSED">CLOSED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
          <button
            class="btn-primary"
            :disabled="statusLoading"
            @click="updateMatchStatus"
          >
            변경
          </button>
        </div>
        <p v-if="statusMsg" :class="statusMsgError ? 'msg-error' : 'msg-ok'">
          {{ statusMsg }}
        </p>
      </div>

      <div class="card">
        <h2>좌석 일괄 등록</h2>
        <div class="row-form mb-s">
          <label style="flex: 0 0 auto">경기 ID</label>
          <input
            v-model.number="seatMatchId"
            type="number"
            placeholder="경기 ID"
            style="width: 120px"
          />
          <button class="btn-secondary" @click="addSeatRow">+ 좌석 추가</button>
        </div>
        <table v-if="seatRows.length" class="data-table mb-s">
          <thead>
            <tr>
              <th>좌석 번호</th>
              <th>등급</th>
              <th>가격</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, i) in seatRows" :key="i">
              <td><input v-model="row.seatNumber" placeholder="A-1" /></td>
              <td><input v-model="row.tier" placeholder="STANDARD" /></td>
              <td>
                <input
                  v-model.number="row.price"
                  type="number"
                  min="1"
                  placeholder="30000"
                />
              </td>
              <td>
                <button class="btn-danger-sm" @click="seatRows.splice(i, 1)">
                  ✕
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <button
          class="btn-primary"
          :disabled="seatLoading || !seatRows.length"
          @click="bulkCreateSeats"
        >
          등록
        </button>
        <p v-if="seatMsg" :class="seatMsgError ? 'msg-error' : 'msg-ok'">
          {{ seatMsg }}
        </p>
      </div>
    </section>

    <!-- ===== 예약 관리 탭 ===== -->
    <section v-if="activeTab === 'reservations'" class="tab-content">
      <div class="card">
        <h2>예약 목록</h2>
        <div class="filter-row">
          <select v-model="resFilter.status">
            <option value="">전체 상태</option>
            <option value="PENDING">PENDING</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
          <input
            v-model.number="resFilter.userId"
            type="number"
            placeholder="사용자 ID"
          />
          <input
            v-model.number="resFilter.matchId"
            type="number"
            placeholder="경기 ID"
          />
          <button class="btn-primary" @click="loadReservations(0)">검색</button>
        </div>

        <div v-if="resLoading" class="loading">불러오는 중...</div>
        <table v-else-if="reservations.length" class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>사용자</th>
              <th>경기</th>
              <th>좌석</th>
              <th>등급</th>
              <th>가격</th>
              <th>상태</th>
              <th>예약일</th>
              <th>취소</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in reservations" :key="r.id">
              <td>{{ r.id }}</td>
              <td>{{ r.userId }}</td>
              <td>{{ r.matchTitle }}</td>
              <td>{{ r.seatNumber }}</td>
              <td>{{ r.tier }}</td>
              <td>{{ r.price.toLocaleString() }}원</td>
              <td>
                <span :class="statusClass(r.status)">{{ r.status }}</span>
              </td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td>
                <button
                  v-if="r.status !== 'CANCELLED'"
                  class="btn-danger-sm"
                  @click="cancelReservation(r.id)"
                >
                  강제취소
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else-if="!resLoading" class="empty">예약 내역이 없습니다.</p>

        <div v-if="resTotalPages > 1" class="pagination">
          <button
            :disabled="resPage === 0"
            @click="loadReservations(resPage - 1)"
          >
            이전
          </button>
          <span>{{ resPage + 1 }} / {{ resTotalPages }}</span>
          <button
            :disabled="resPage >= resTotalPages - 1"
            @click="loadReservations(resPage + 1)"
          >
            다음
          </button>
        </div>
      </div>
    </section>

    <!-- ===== 사용자 관리 탭 ===== -->
    <section v-if="activeTab === 'users'" class="tab-content">
      <div class="card">
        <h2>사용자 목록</h2>
        <div v-if="usersLoading" class="loading">불러오는 중...</div>
        <table v-else-if="users.length" class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>이메일</th>
              <th>역할</th>
              <th>상태</th>
              <th>활성예약</th>
              <th>취소예약</th>
              <th>역할변경</th>
              <th>상태변경</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.name }}</td>
              <td>{{ u.email }}</td>
              <td>
                <span
                  :class="u.role === 'ADMIN' ? 'badge-admin' : 'badge-user'"
                  >{{ u.role }}</span
                >
              </td>
              <td>
                <span :class="userStatusClass(u.status)">{{ u.status }}</span>
              </td>
              <td>{{ u.activeReservations }}</td>
              <td>{{ u.cancelledReservations }}</td>
              <td>
                <select
                  :value="u.role"
                  class="select-sm"
                  @change="
                    (e) =>
                      updateUserRole(
                        u.id,
                        (e.target as HTMLSelectElement).value as
                          | 'USER'
                          | 'ADMIN',
                      )
                  "
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td>
                <select
                  :value="u.status"
                  class="select-sm"
                  @change="
                    (e) =>
                      updateUserStatus(
                        u.id,
                        (e.target as HTMLSelectElement).value as
                          | 'active'
                          | 'inactive'
                          | 'suspended'
                          | 'blacklisted',
                      )
                  "
                >
                  <option value="active">active</option>
                  <option value="inactive">inactive</option>
                  <option value="suspended">suspended</option>
                  <option value="blacklisted">blacklisted</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else-if="!usersLoading" class="empty">사용자가 없습니다.</p>

        <div v-if="usersTotalPages > 1" class="pagination">
          <button :disabled="usersPage === 0" @click="loadUsers(usersPage - 1)">
            이전
          </button>
          <span>{{ usersPage + 1 }} / {{ usersTotalPages }}</span>
          <button
            :disabled="usersPage >= usersTotalPages - 1"
            @click="loadUsers(usersPage + 1)"
          >
            다음
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { adminMatchApi, adminReservationApi, adminUserApi } from "@/api";
import type {
  AdminReservationResponse,
  MatchStatus,
  ReservationStatus,
  UserRole,
  UserStatus,
  UserSummaryResponse,
} from "@/api/types";

// ── 탭 ──────────────────────────────────────────────
const tabs = [
  { key: "matches", label: "경기 관리" },
  { key: "reservations", label: "예약 관리" },
  { key: "users", label: "사용자 관리" },
] as const;
type TabKey = (typeof tabs)[number]["key"];
const activeTab = ref<TabKey>("matches");

// 탭 전환 시 데이터 로드
watch(activeTab, (tab) => {
  if (tab === "reservations") loadReservations(0);
  if (tab === "users") loadUsers(0);
});

// ── 경기 등록 ─────────────────────────────────────
const matchForm = ref({ title: "", matchDate: "", stadiumName: "" });
const matchLoading = ref(false);
const matchMsg = ref("");
const matchMsgError = ref(false);

async function createMatch() {
  matchLoading.value = true;
  matchMsg.value = "";
  try {
    await adminMatchApi.createMatch({
      ...matchForm.value,
      matchDate: matchForm.value.matchDate + ":00",
    });
    matchMsg.value = "경기가 등록되었습니다.";
    matchMsgError.value = false;
    matchForm.value = { title: "", matchDate: "", stadiumName: "" };
  } catch {
    matchMsg.value = "경기 등록에 실패했습니다.";
    matchMsgError.value = true;
  } finally {
    matchLoading.value = false;
  }
}

// ── 경기 상태 변경 ────────────────────────────────
const statusMatchId = ref<number | null>(null);
const statusMatchValue = ref<MatchStatus>("ON_SALE");
const statusLoading = ref(false);
const statusMsg = ref("");
const statusMsgError = ref(false);

async function updateMatchStatus() {
  if (!statusMatchId.value) return;
  statusLoading.value = true;
  statusMsg.value = "";
  try {
    await adminMatchApi.updateMatchStatus(
      statusMatchId.value,
      statusMatchValue.value,
    );
    statusMsg.value = "상태가 변경되었습니다.";
    statusMsgError.value = false;
  } catch {
    statusMsg.value = "상태 변경에 실패했습니다.";
    statusMsgError.value = true;
  } finally {
    statusLoading.value = false;
  }
}

// ── 좌석 일괄 등록 ────────────────────────────────
const seatMatchId = ref<number | null>(null);
const seatRows = ref<
  Array<{ seatNumber: string; tier: string; price: number }>
>([]);
const seatLoading = ref(false);
const seatMsg = ref("");
const seatMsgError = ref(false);

function addSeatRow() {
  seatRows.value.push({ seatNumber: "", tier: "", price: 0 });
}

async function bulkCreateSeats() {
  if (!seatMatchId.value) {
    seatMsg.value = "경기 ID를 입력하세요.";
    seatMsgError.value = true;
    return;
  }
  seatLoading.value = true;
  seatMsg.value = "";
  try {
    const res = await adminMatchApi.bulkCreateSeats(seatMatchId.value, {
      seats: seatRows.value,
    });
    seatMsg.value = `${res.data.length}개 좌석이 등록되었습니다.`;
    seatMsgError.value = false;
    seatRows.value = [];
  } catch {
    seatMsg.value = "좌석 등록에 실패했습니다.";
    seatMsgError.value = true;
  } finally {
    seatLoading.value = false;
  }
}

// ── 예약 관리 ─────────────────────────────────────
const resFilter = ref<{
  status: ReservationStatus | "";
  userId: number | null;
  matchId: number | null;
}>({
  status: "",
  userId: null,
  matchId: null,
});
const reservations = ref<AdminReservationResponse[]>([]);
const resLoading = ref(false);
const resPage = ref(0);
const resTotalPages = ref(0);

async function loadReservations(page: number) {
  resLoading.value = true;
  resPage.value = page;
  try {
    const params: Record<string, unknown> = { page, size: 20 };
    if (resFilter.value.status) params.status = resFilter.value.status;
    if (resFilter.value.userId) params.userId = resFilter.value.userId;
    if (resFilter.value.matchId) params.matchId = resFilter.value.matchId;
    const res = await adminReservationApi.getReservations(
      params as Parameters<typeof adminReservationApi.getReservations>[0],
    );
    reservations.value = res.data.content;
    resTotalPages.value = res.data.totalPages;
  } catch {
    reservations.value = [];
  } finally {
    resLoading.value = false;
  }
}

async function cancelReservation(id: number) {
  if (!confirm(`예약 #${id}를 강제 취소하시겠습니까?`)) return;
  try {
    await adminReservationApi.cancelReservation(id);
    await loadReservations(resPage.value);
  } catch {
    alert("취소에 실패했습니다.");
  }
}

// ── 사용자 관리 ───────────────────────────────────
const users = ref<UserSummaryResponse[]>([]);
const usersLoading = ref(false);
const usersPage = ref(0);
const usersTotalPages = ref(0);

async function loadUsers(page: number) {
  usersLoading.value = true;
  usersPage.value = page;
  try {
    const res = await adminUserApi.getUsers(page, 20);
    users.value = res.data.content;
    usersTotalPages.value = res.data.totalPages;
  } catch {
    users.value = [];
  } finally {
    usersLoading.value = false;
  }
}

async function updateUserRole(id: number, role: UserRole) {
  try {
    await adminUserApi.updateUserRole(id, role);
    const u = users.value.find((u) => u.id === id);
    if (u) u.role = role;
  } catch {
    alert("역할 변경에 실패했습니다.");
  }
}

async function updateUserStatus(id: number, status: UserStatus) {
  try {
    await adminUserApi.updateUserStatus(id, status);
    const u = users.value.find((u) => u.id === id);
    if (u) u.status = status;
  } catch {
    alert("상태 변경에 실패했습니다.");
  }
}

// ── 헬퍼 ─────────────────────────────────────────
function formatDate(iso: string) {
  return new Date(iso).toLocaleString("ko-KR", { hour12: false });
}

function statusClass(s: ReservationStatus) {
  return {
    "badge-confirmed": s === "CONFIRMED",
    "badge-pending": s === "PENDING",
    "badge-cancelled": s === "CANCELLED",
  };
}

function userStatusClass(s: UserStatus) {
  return {
    "badge-active": s === "active",
    "badge-inactive": s === "inactive",
    "badge-suspended": s === "suspended",
    "badge-blacklisted": s === "blacklisted",
  };
}
</script>

<style scoped>
.admin-page {
  max-width: 1100px;
  margin: 80px auto 40px;
  padding: 0 20px;
}

.page-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1565c0;
  margin-bottom: 24px;
}

/* ── 탭 ── */
.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid #e3f2fd;
  margin-bottom: 24px;
}

.tab-btn {
  padding: 10px 24px;
  border: none;
  background: none;
  font-size: 0.95rem;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  border-radius: 4px 4px 0 0;
  transition: all 0.15s;
}

.tab-btn:hover {
  color: #1565c0;
  background: #e3f2fd;
}
.tab-btn.active {
  color: #1565c0;
  font-weight: 700;
  border-bottom-color: #1565c0;
}

/* ── 카드 ── */
.card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.card h2 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

/* ── 폼 ── */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
  gap: 12px;
  align-items: end;
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.85rem;
  color: #555;
}

.form-grid input {
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
}

.row-form {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.row-form input,
.row-form select {
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
}

.filter-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.filter-row input,
.filter-row select {
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
}

/* ── 버튼 ── */
.btn-primary {
  padding: 8px 20px;
  background: #1565c0;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: background 0.15s;
}
.btn-primary:hover {
  background: #0d47a1;
}
.btn-primary:disabled {
  background: #90caf9;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 8px 16px;
  background: #fff;
  color: #1565c0;
  border: 1px solid #1565c0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.15s;
}
.btn-secondary:hover {
  background: #e3f2fd;
}

.btn-danger-sm {
  padding: 4px 10px;
  background: #e53935;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}
.btn-danger-sm:hover {
  background: #c62828;
}

/* ── 테이블 ── */
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.data-table th {
  background: #f5f5f5;
  padding: 10px 8px;
  text-align: left;
  color: #555;
  font-weight: 600;
  border-bottom: 2px solid #e0e0e0;
}

.data-table td {
  padding: 9px 8px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.data-table td input {
  width: 100%;
  padding: 5px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.85rem;
}

.select-sm {
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.83rem;
}

/* ── 배지 ── */
.badge-confirmed {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
}
.badge-pending {
  background: #fff8e1;
  color: #f57f17;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
}
.badge-cancelled {
  background: #fce4ec;
  color: #c62828;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
}

.badge-admin {
  background: #fce4ec;
  color: #c62828;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 700;
}
.badge-user {
  background: #e3f2fd;
  color: #1565c0;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
}

.badge-active {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
}
.badge-inactive {
  background: #f5f5f5;
  color: #757575;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
}
.badge-suspended {
  background: #fff8e1;
  color: #f57f17;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
}
.badge-blacklisted {
  background: #212121;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
}

/* ── 기타 ── */
.pagination {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  margin-top: 12px;
}

.pagination button {
  padding: 6px 16px;
  border: 1px solid #ccc;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}
.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.msg-ok {
  color: #2e7d32;
  font-size: 0.88rem;
  margin-top: 8px;
}
.msg-error {
  color: #c62828;
  font-size: 0.88rem;
  margin-top: 8px;
}

.loading {
  color: #888;
  padding: 20px 0;
  text-align: center;
}
.empty {
  color: #aaa;
  text-align: center;
  padding: 20px 0;
}
.mb-s {
  margin-bottom: 10px;
}
</style>
