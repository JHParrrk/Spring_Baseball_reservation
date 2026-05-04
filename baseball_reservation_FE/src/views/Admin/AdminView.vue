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
            <input
              v-model="matchForm.title"
              placeholder="경기 제목"
              maxlength="120"
              required
            />
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
            <select
              v-model="matchForm.stadiumName"
              required
              @change="onSelectStadium"
            >
              <option value="" disabled>구장 선택</option>
              <option
                v-for="template in stadiumTemplates"
                :key="template.stadiumName"
                :value="template.stadiumName"
              >
                {{ template.stadiumName }} ({{ template.seatCount }}석)
              </option>
            </select>
          </label>
          <button type="submit" class="ui-btn-primary" :disabled="matchLoading">
            등록
          </button>
        </form>
        <div v-if="stadiumTemplatesLoading" class="loading">
          구장 템플릿 로딩 중...
        </div>
        <div v-else-if="selectedStadiumTemplate" class="template-preview">
          <div class="template-preview-head">
            <strong>{{ selectedStadiumTemplate.stadiumName }}</strong>
            <span>{{ selectedStadiumTemplate.seatCount }}석</span>
          </div>
          <div class="seat-blocks-list">
            <div
              v-for="block in groupedBlocks"
              :key="`${block.prefix}-${block.tier}-${block.price}`"
              class="seat-block"
            >
              <p class="block-info">
                {{ block.prefix }}, {{ block.count }}석, {{ block.tier }},
                {{ block.price.toLocaleString() }}원
              </p>
            </div>
          </div>
        </div>
        <p v-if="matchMsg" :class="matchMsgError ? 'msg-error' : 'msg-ok'">
          {{ matchMsg }}
        </p>
      </div>

      <div class="card">
        <h2>경기 상태 변경</h2>
        <div v-if="adminMatchesLoading" class="loading">불러오는 중...</div>
        <table v-else-if="adminMatches.length" class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>제목</th>
              <th>경기일</th>
              <th>경기장</th>
              <th>현재 상태</th>
              <th>변경할 상태</th>
              <th>적용</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in adminMatches" :key="m.id">
              <td>{{ m.id }}</td>
              <td>{{ m.title }}</td>
              <td>{{ formatDate(m.matchDate) }}</td>
              <td>{{ m.stadiumName }}</td>
              <td>{{ m.status }}</td>
              <td>
                <select
                  class="select-sm"
                  :value="statusTargets[m.id]"
                  :disabled="allowedTransitionsFor(m.status).length === 0"
                  @change="
                    handleMatchStatusChange(
                      m.id,
                      ($event.target as HTMLSelectElement).value,
                    )
                  "
                >
                  <option
                    v-for="next in allowedTransitionsFor(m.status)"
                    :key="`${m.id}-${next}`"
                    :value="next"
                  >
                    {{ next }}
                  </option>
                </select>
              </td>
              <td>
                <button
                  class="ui-btn-primary"
                  :disabled="
                    statusLoading ||
                    allowedTransitionsFor(m.status).length === 0
                  "
                  @click="updateMatchStatus(m.id)"
                >
                  변경
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">등록된 경기가 없습니다.</p>

        <div v-if="adminMatchesTotalPages > 1" class="pagination">
          <button
            :disabled="adminMatchesPage === 0"
            @click="loadAdminMatches(adminMatchesPage - 1)"
          >
            이전
          </button>
          <span>{{ adminMatchesPage + 1 }} / {{ adminMatchesTotalPages }}</span>
          <button
            :disabled="adminMatchesPage >= adminMatchesTotalPages - 1"
            @click="loadAdminMatches(adminMatchesPage + 1)"
          >
            다음
          </button>
        </div>
        <p v-if="statusMsg" :class="statusMsgError ? 'msg-error' : 'msg-ok'">
          {{ statusMsg }}
        </p>
      </div>

      <div class="card">
        <h2>신규 구장 좌석 템플릿 등록</h2>
        <div class="row-form mb-s">
          <label class="stadium-name-label">구장 이름</label>
          <input
            v-model="newTemplateStadiumName"
            placeholder="예: 잠실야구장"
            maxlength="100"
            class="stadium-name-input"
          />
        </div>

        <div class="auto-gen-grid mb-s">
          <label>
            좌석 이름(접두어)
            <input
              v-model="autoSeatPrefix"
              placeholder="예: 일반석"
              maxlength="30"
            />
          </label>
          <label>
            수량
            <input
              v-model.number="autoSeatCount"
              type="number"
              min="1"
              placeholder="200"
            />
          </label>
          <label>
            시작 번호
            <input
              v-model.number="autoSeatStartNo"
              type="number"
              min="1"
              placeholder="1"
            />
          </label>
          <label>
            등급
            <input v-model="autoSeatTier" placeholder="STANDARD" />
          </label>
          <label>
            가격
            <input
              v-model.number="autoSeatPrice"
              type="number"
              min="1"
              placeholder="15000"
            />
          </label>
        </div>

        <div class="row-form mb-s">
          <button class="ui-btn-secondary" @click="addGeneratedSeats">
            생성
          </button>
          <button class="ui-btn-danger-sm" @click="autoGenerateList = []">
            초기화
          </button>
        </div>

        <!-- 자동생성 미리보기 리스트 -->
        <div v-if="autoGenerateList.length" class="preview-list mb-s">
          <div
            v-for="(block, idx) in autoGenerateList"
            :key="idx"
            class="preview-item"
          >
            <p class="preview-text">
              <strong>{{ block.prefix }}</strong>
              {{ block.startNo }}-{{ block.startNo + block.count - 1 }} ({{
                block.count
              }}석, {{ block.price.toLocaleString() }}원)
            </p>
            <button class="ui-btn-danger-sm" @click="removeGeneratedBlock(idx)">
              ✕
            </button>
          </div>
        </div>

        <button
          class="ui-btn-primary"
          :disabled="seatLoading || !autoGenerateList.length"
          @click="createStadiumTemplate"
        >
          템플릿 등록
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
            <option
              v-for="status in reservationStatusOptions"
              :key="status"
              :value="status"
            >
              {{ status }}
            </option>
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
          <button class="ui-btn-primary" @click="loadReservations(0)">
            검색
          </button>
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
                <template v-if="r.status !== 'CANCELLED'">
                  <button
                    v-if="confirmingResId !== r.id"
                    class="ui-btn-danger-sm"
                    @click="confirmingResId = r.id"
                  >
                    강제취소
                  </button>
                  <span v-else class="confirm-inline">
                    취소?
                    <button
                      class="ui-btn-danger-sm"
                      @click="cancelReservation(r.id)"
                    >
                      확인
                    </button>
                    <button
                      class="btn-xs-neutral"
                      @click="confirmingResId = null"
                    >
                      아니요
                    </button>
                  </span>
                </template>
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
        <p v-if="resMsg" :class="resMsgError ? 'msg-error' : 'msg-ok'">
          {{ resMsg }}
        </p>
      </div>
    </section>
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
                  :class="
                    u.role === 'ADMIN' ? 'ui-badge-admin' : 'ui-badge-user'
                  "
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
                      handleUserRoleChange(
                        u.id,
                        (e.target as HTMLSelectElement).value,
                      )
                  "
                >
                  <option
                    v-for="role in userRoleOptions"
                    :key="role"
                    :value="role"
                  >
                    {{ role }}
                  </option>
                </select>
              </td>
              <td>
                <select
                  :value="u.status"
                  class="select-sm"
                  @change="
                    (e) =>
                      handleUserStatusChange(
                        u.id,
                        (e.target as HTMLSelectElement).value,
                      )
                  "
                >
                  <option
                    v-for="status in userStatusOptions"
                    :key="status"
                    :value="status"
                  >
                    {{ status }}
                  </option>
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
        <p v-if="userMsg" :class="userMsgError ? 'msg-error' : 'msg-ok'">
          {{ userMsg }}
        </p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import "./AdminView.css";
import { ref, watch, computed } from "vue";
import { adminMatchApi, adminReservationApi, adminUserApi } from "@/api/admin";
import type {
  AdminReservationResponse,
  MatchResponse,
  MatchStatus,
  ReservationStatus,
  StadiumTemplateDetailResponse,
  StadiumTemplateSummaryResponse,
  UserRole,
  UserStatus,
  UserSummaryResponse,
} from "@/api/types";
import { formatDate } from "@/utils/format";
import { useFormMsg } from "@/composables/useFormMsg";
import { extractApiError } from "@/utils/apiError";

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
  if (tab === "matches") {
    void loadAdminMatches(0);
    void loadStadiumTemplates();
  }
  if (tab === "reservations") void loadReservations(0);
  if (tab === "users") void loadUsers(0);
});

// ── 경기 등록 ─────────────────────────────────────
const matchForm = ref({ title: "", matchDate: "", stadiumName: "" });
const matchLoading = ref(false);
const {
  msg: matchMsg,
  isError: matchMsgError,
  setMsg: setMatchMsg,
} = useFormMsg();
const stadiumTemplates = ref<StadiumTemplateSummaryResponse[]>([]);
const stadiumTemplatesLoading = ref(false);
const selectedStadiumTemplate = ref<StadiumTemplateDetailResponse | null>(null);

async function loadStadiumTemplates(): Promise<void> {
  stadiumTemplatesLoading.value = true;
  setMatchMsg("");
  try {
    const res = await adminMatchApi.getStadiumTemplates();
    stadiumTemplates.value = res.data;

    if (!stadiumTemplates.value.length) {
      matchForm.value.stadiumName = "";
      selectedStadiumTemplate.value = null;
      return;
    }

    if (!matchForm.value.stadiumName) {
      matchForm.value.stadiumName = stadiumTemplates.value[0].stadiumName;
    }
    await loadStadiumTemplateDetail(matchForm.value.stadiumName);
  } catch {
    stadiumTemplates.value = [];
    selectedStadiumTemplate.value = null;
    setMatchMsg("구장 템플릿을 불러오지 못했습니다.", true);
  } finally {
    stadiumTemplatesLoading.value = false;
  }
}

async function loadStadiumTemplateDetail(stadiumName: string) {
  if (!stadiumName) {
    selectedStadiumTemplate.value = null;
    return;
  }
  try {
    const res = await adminMatchApi.getStadiumTemplateDetail(stadiumName);
    selectedStadiumTemplate.value = res.data;
  } catch {
    selectedStadiumTemplate.value = null;
  }
}

function onSelectStadium() {
  void loadStadiumTemplateDetail(matchForm.value.stadiumName);
}

// 좌석을 좌석명별로 집계하는 computed
const groupedBlocks = computed(() => {
  if (!selectedStadiumTemplate.value?.seats) return [];

  const seats = selectedStadiumTemplate.value.seats;
  const groups = new Map<
    string,
    {
      prefix: string;
      tier: string;
      price: number;
      count: number;
    }
  >();

  for (const seat of seats) {
    const regexResult = seat.seatNumber.match(/^(.+?)-(\d+)$/);
    if (!regexResult) continue;

    const prefix = regexResult[1];
    const tier = seat.tier;
    const price = seat.price;
    const key = `${prefix}|${tier}|${price}`;

    if (groups.has(key)) {
      const group = groups.get(key)!;
      group.count += 1;
    } else {
      groups.set(key, { prefix, tier, price, count: 1 });
    }
  }

  return Array.from(groups.values());
});

async function createMatch() {
  if (!matchForm.value.stadiumName) {
    setMatchMsg("구장 템플릿을 먼저 선택하세요.", true);
    return;
  }

  matchLoading.value = true;
  setMatchMsg("");
  try {
    await adminMatchApi.createMatch({
      ...matchForm.value,
      matchDate: matchForm.value.matchDate + ":00",
    });
    setMatchMsg("경기가 등록되었습니다.");
    matchForm.value = {
      title: "",
      matchDate: "",
      stadiumName: matchForm.value.stadiumName,
    };
    await loadAdminMatches(0);
  } catch (e: unknown) {
    setMatchMsg(extractApiError(e, "경기 등록에 실패했습니다."), true);
  } finally {
    matchLoading.value = false;
  }
}

// 상태 전이 규칙 (백엔드 StateMachine과 동일)
const TRANSITIONS: Record<string, MatchStatus[]> = {
  UPCOMING: ["ON_SALE", "CANCELLED"],
  ON_SALE: ["CLOSED", "CANCELLED"],
  CLOSED: [],
  CANCELLED: [],
};
const matchStatusOptions: MatchStatus[] = [
  "UPCOMING",
  "ON_SALE",
  "CLOSED",
  "CANCELLED",
];
const reservationStatusOptions: ReservationStatus[] = [
  "PENDING",
  "CONFIRMED",
  "CANCELLED",
];
const userRoleOptions: UserRole[] = ["USER", "ADMIN"];
const userStatusOptions: UserStatus[] = [
  "active",
  "inactive",
  "suspended",
  "blacklisted",
];

// ── 경기 상태 변경(리스트 기반) ─────────────────────
const adminMatches = ref<MatchResponse[]>([]);
const adminMatchesLoading = ref(false);
const adminMatchesPage = ref(0);
const adminMatchesTotalPages = ref(0);
const statusTargets = ref<Record<number, MatchStatus>>({});
const statusLoading = ref(false);
const {
  msg: statusMsg,
  isError: statusMsgError,
  setMsg: setStatusMsg,
} = useFormMsg();

function allowedTransitionsFor(current: MatchStatus): MatchStatus[] {
  return TRANSITIONS[current] ?? [];
}

function setStatusTarget(matchId: number, nextStatus: MatchStatus) {
  statusTargets.value[matchId] = nextStatus;
}

function parseMatchStatus(value: string): MatchStatus | null {
  return matchStatusOptions.includes(value as MatchStatus)
    ? (value as MatchStatus)
    : null;
}

function parseUserRole(value: string): UserRole | null {
  return userRoleOptions.includes(value as UserRole)
    ? (value as UserRole)
    : null;
}

function parseUserStatus(value: string): UserStatus | null {
  return userStatusOptions.includes(value as UserStatus)
    ? (value as UserStatus)
    : null;
}

function handleMatchStatusChange(matchId: number, value: string): void {
  const parsed = parseMatchStatus(value);
  if (!parsed) {
    setStatusMsg("유효하지 않은 경기 상태 값입니다.", true);
    return;
  }
  setStatusTarget(matchId, parsed);
}

async function loadAdminMatches(page: number): Promise<void> {
  const safePage = Math.max(0, page);
  adminMatchesLoading.value = true;
  setStatusMsg("");
  adminMatchesPage.value = safePage;
  try {
    const res = await adminMatchApi.getMatches(safePage, 20);
    adminMatches.value = res.data.content;
    adminMatchesTotalPages.value = res.data.totalPages;

    const targets: Record<number, MatchStatus> = {};
    for (const match of adminMatches.value) {
      const allowed = allowedTransitionsFor(match.status);
      if (allowed.length > 0) {
        targets[match.id] = allowed[0];
      }
    }
    statusTargets.value = targets;
  } catch {
    adminMatches.value = [];
    adminMatchesTotalPages.value = 0;
    setStatusMsg("경기 목록을 불러오지 못했습니다.", true);
  } finally {
    adminMatchesLoading.value = false;
  }
}

async function updateMatchStatus(matchId: number) {
  const nextStatus = statusTargets.value[matchId];
  if (!nextStatus) return;

  statusLoading.value = true;
  setStatusMsg("");
  try {
    await adminMatchApi.updateMatchStatus(matchId, nextStatus);
    setStatusMsg("상태가 변경되었습니다.");
    await loadAdminMatches(adminMatchesPage.value);
  } catch (e: unknown) {
    setStatusMsg(extractApiError(e, "상태 변경에 실패했습니다."), true);
  } finally {
    statusLoading.value = false;
  }
}

// ── 좌석 일괄 등록 ────────────────────────────────
const newTemplateStadiumName = ref("");
const seatLoading = ref(false);
const {
  msg: seatMsg,
  isError: seatMsgError,
  setMsg: setSeatMsg,
} = useFormMsg();
const autoSeatPrefix = ref("일반석");
const autoSeatCount = ref<number | null>(null);
const autoSeatStartNo = ref(1);
const autoSeatTier = ref("STANDARD");
const autoSeatPrice = ref<number | null>(null);
const autoGenerateList = ref<
  Array<{
    prefix: string;
    count: number;
    startNo: number;
    tier: string;
    price: number;
  }>
>([]);

function addGeneratedSeats() {
  const prefix = autoSeatPrefix.value.trim();
  const count = autoSeatCount.value ?? 0;
  const startNo = autoSeatStartNo.value ?? 1;
  const tier = autoSeatTier.value.trim();
  const price = autoSeatPrice.value ?? 0;

  if (!prefix || !tier || count <= 0 || startNo <= 0 || price <= 0) {
    setSeatMsg(
      "좌석 이름/수량/시작번호/등급/가격을 올바르게 입력하세요.",
      true,
    );
    return;
  }

  // 미리보기 리스트에 추가
  autoGenerateList.value.push({ prefix, count, startNo, tier, price });
  setSeatMsg(
    `${prefix} ${startNo}-${startNo + count - 1} 블록이 추가되었습니다.`,
  );
  // 입력값 초기화
  autoSeatPrefix.value = "일반석";
  autoSeatCount.value = null;
  autoSeatStartNo.value = 1;
  autoSeatTier.value = "STANDARD";
  autoSeatPrice.value = null;
}

function removeGeneratedBlock(index: number) {
  autoGenerateList.value.splice(index, 1);
  setSeatMsg("블록이 제거되었습니다.");
}

async function createStadiumTemplate() {
  if (!newTemplateStadiumName.value.trim()) {
    setSeatMsg("구장 이름을 입력하세요.", true);
    return;
  }

  const seats: Array<{ seatNumber: string; tier: string; price: number }> = [];
  for (const block of autoGenerateList.value) {
    for (let i = 0; i < block.count; i += 1) {
      seats.push({
        seatNumber: `${block.prefix}-${block.startNo + i}`,
        tier: block.tier,
        price: block.price,
      });
    }
  }
  autoGenerateList.value = [];

  if (!seats.length) {
    setSeatMsg("최소 1개 이상의 좌석을 등록하세요.", true);
    return;
  }

  seatLoading.value = true;
  setSeatMsg("");
  try {
    const res = await adminMatchApi.createStadiumTemplate({
      stadiumName: newTemplateStadiumName.value.trim(),
      seats,
    });

    setSeatMsg(
      `${res.data.stadiumName} 템플릿이 등록되었습니다. (${res.data.seatCount}석)`,
    );
    newTemplateStadiumName.value = "";
    await loadStadiumTemplates();
    matchForm.value.stadiumName = res.data.stadiumName;
    await loadStadiumTemplateDetail(res.data.stadiumName);
  } catch (e: unknown) {
    setSeatMsg(extractApiError(e, "구장 템플릿 등록에 실패했습니다."), true);
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
const { msg: resMsg, isError: resMsgError, setMsg: setResMsg } = useFormMsg();
const confirmingResId = ref<number | null>(null);

async function loadReservations(page: number) {
  const safePage = Math.max(0, page);
  resLoading.value = true;
  setResMsg("");
  resPage.value = safePage;
  confirmingResId.value = null;
  try {
    const res = await adminReservationApi.getReservations({
      page: safePage,
      size: 20,
      ...(resFilter.value.status ? { status: resFilter.value.status } : {}),
      ...(resFilter.value.userId ? { userId: resFilter.value.userId } : {}),
      ...(resFilter.value.matchId ? { matchId: resFilter.value.matchId } : {}),
    });
    reservations.value = res.data.content;
    resTotalPages.value = res.data.totalPages;
  } catch {
    reservations.value = [];
    resTotalPages.value = 0;
    setResMsg("예약 목록을 불러오지 못했습니다.", true);
  } finally {
    resLoading.value = false;
  }
}

async function cancelReservation(id: number) {
  confirmingResId.value = null;
  try {
    await adminReservationApi.cancelReservation(id);
    setResMsg("");
    await loadReservations(resPage.value);
  } catch (e: unknown) {
    setResMsg(extractApiError(e, "강제 취소에 실패했습니다."), true);
  }
}

// ── 사용자 관리 ───────────────────────────────────
const users = ref<UserSummaryResponse[]>([]);
const usersLoading = ref(false);
const usersPage = ref(0);
const usersTotalPages = ref(0);
const {
  msg: userMsg,
  isError: userMsgError,
  setMsg: setUserMsg,
} = useFormMsg();
async function loadUsers(page: number) {
  const maxPage = Math.max(0, usersTotalPages.value - 1);
  const safePage = Math.max(0, Math.min(page, maxPage || page));
  usersLoading.value = true;
  setUserMsg("");
  usersPage.value = safePage;
  try {
    const res = await adminUserApi.getUsers(safePage, 20);
    users.value = res.data.content;
    usersTotalPages.value = res.data.totalPages;
  } catch {
    users.value = [];
    usersTotalPages.value = 0;
    setUserMsg("사용자 목록을 불러오지 못했습니다.", true);
  } finally {
    usersLoading.value = false;
  }
}

async function updateUserRole(id: number, role: UserRole) {
  try {
    await adminUserApi.updateUserRole(id, role);
    const u = users.value.find((u) => u.id === id);
    if (u) u.role = role;
  } catch (e: unknown) {
    setUserMsg(extractApiError(e, "역할 변경에 실패했습니다."), true);
  }
}

async function handleUserRoleChange(id: number, value: string): Promise<void> {
  const role = parseUserRole(value);
  if (!role) {
    setUserMsg("유효하지 않은 사용자 역할 값입니다.", true);
    return;
  }
  await updateUserRole(id, role);
}

async function updateUserStatus(id: number, status: UserStatus) {
  try {
    await adminUserApi.updateUserStatus(id, status);
    const u = users.value.find((u) => u.id === id);
    if (u) u.status = status;
  } catch (e: unknown) {
    setUserMsg(extractApiError(e, "상태 변경에 실패했습니다."), true);
  }
}

async function handleUserStatusChange(
  id: number,
  value: string,
): Promise<void> {
  const status = parseUserStatus(value);
  if (!status) {
    setUserMsg("유효하지 않은 사용자 상태 값입니다.", true);
    return;
  }
  await updateUserStatus(id, status);
}

// ── 헬퍼 ─────────────────────────────────────────
function statusClass(s: ReservationStatus) {
  return {
    "ui-badge-confirmed": s === "CONFIRMED",
    "ui-badge-pending": s === "PENDING",
    "ui-badge-cancelled": s === "CANCELLED",
  };
}

function userStatusClass(s: UserStatus) {
  return {
    "ui-badge-active": s === "active",
    "ui-badge-inactive": s === "inactive",
    "ui-badge-suspended": s === "suspended",
    "ui-badge-blacklisted": s === "blacklisted",
  };
}

void loadAdminMatches(0);
void loadStadiumTemplates();
</script>
