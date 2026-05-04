import { computed, ref } from "vue";

/**
 * 페이지 이동에 필요한 공통 상태/동작을 관리하는 Composable.
 */
export function usePagination(initialPage = 0, initialTotalPages = 1) {
  const currentPage = ref(initialPage);
  const totalPages = ref(initialTotalPages);

  const canPrev = computed(() => currentPage.value > 0);
  const canNext = computed(() => currentPage.value < totalPages.value - 1);

  function goTo(page: number): number {
    const maxPage = Math.max(0, totalPages.value - 1);
    const safePage = Math.max(0, Math.min(page, maxPage));
    currentPage.value = safePage;
    return safePage;
  }

  function setFromResponse(page: number, total: number): void {
    totalPages.value = Math.max(1, total);
    goTo(page);
  }

  function next(): number {
    return goTo(currentPage.value + 1);
  }

  function prev(): number {
    return goTo(currentPage.value - 1);
  }

  return {
    currentPage,
    totalPages,
    canPrev,
    canNext,
    goTo,
    setFromResponse,
    next,
    prev,
  };
}
