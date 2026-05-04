import { ref } from "vue";

/**
 * 폼 내부의 인라인 피드백 메시지 상태 관리 Composable.
 * 성공/실패 메시지를 단일 ref 쌍으로 관리합니다.
 *
 * @example
 * const { msg, isError, setMsg, clearMsg } = useFormMsg();
 * setMsg("등록되었습니다.");          // 성공
 * setMsg("등록에 실패했습니다.", true); // 에러
 */
export function useFormMsg() {
  const msg = ref("");
  const isError = ref(false);

  function setMsg(message: string, error = false): void {
    msg.value = message;
    isError.value = error;
  }

  function clearMsg(): void {
    msg.value = "";
    isError.value = false;
  }

  return { msg, isError, setMsg, clearMsg };
}
