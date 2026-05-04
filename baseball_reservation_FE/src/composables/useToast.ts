import { ref, onUnmounted } from "vue";

export interface ToastMessage {
  type: "success" | "error";
  message: string;
}

/**
 * 일시적으로 표시되는 Toast 메시지 관리 Composable.
 *
 * @example
 * const { toast, showToast } = useToast();
 * showToast("success", "처리되었습니다.");
 */
export function useToast(durationMs = 5000) {
  const toast = ref<ToastMessage | null>(null);
  let timer: ReturnType<typeof setTimeout> | null = null;

  function showToast(type: ToastMessage["type"], message: string): void {
    if (timer) clearTimeout(timer);
    toast.value = { type, message };
    timer = setTimeout(() => {
      toast.value = null;
      timer = null;
    }, durationMs);
  }

  function clearToast(): void {
    if (timer) clearTimeout(timer);
    timer = null;
    toast.value = null;
  }

  onUnmounted(() => {
    clearToast();
  });

  return { toast, showToast, clearToast };
}
