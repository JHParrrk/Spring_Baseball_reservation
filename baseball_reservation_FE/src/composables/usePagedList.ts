import { ref } from "vue";
import type { Ref } from "vue";
import type { PageResponse } from "@/api/types";
import type { AxiosResponse } from "axios";
import { usePagination } from "@/composables/usePagination";

/**
 * 페이지네이션이 있는 목록 데이터 관리 Composable.
 *
 * @param fetchFn - 페이지 번호를 받아 PageResponse를 반환하는 API 함수
 *
 * @example
 * const { items, currentPage, totalPages, loading, error, loadPage } =
 *   usePagedList((page) => reservationApi.getMyReservations(page));
 */
export function usePagedList<T>(
  fetchFn: (page: number) => Promise<AxiosResponse<PageResponse<T>>>,
  errorMessage = "목록을 불러오지 못했습니다.",
) {
  const items = ref([]) as Ref<T[]>;
  const loading = ref(true);
  const error = ref<string | null>(null);
  const {
    currentPage,
    totalPages,
    canPrev,
    canNext,
    setFromResponse,
    goTo,
    next,
    prev,
  } = usePagination();

  async function loadPage(page: number): Promise<void> {
    const safePage = goTo(page);
    loading.value = true;
    error.value = null;
    try {
      const res = await fetchFn(safePage);
      items.value = res.data.content;
      setFromResponse(res.data.number, res.data.totalPages);
    } catch {
      error.value = errorMessage;
    } finally {
      loading.value = false;
    }
  }

  async function prevPage(): Promise<void> {
    if (!canPrev.value) return;
    await loadPage(prev());
  }

  async function nextPage(): Promise<void> {
    if (!canNext.value) return;
    await loadPage(next());
  }

  return {
    items,
    loading,
    error,
    currentPage,
    totalPages,
    canPrev,
    canNext,
    loadPage,
    prevPage,
    nextPage,
  };
}
