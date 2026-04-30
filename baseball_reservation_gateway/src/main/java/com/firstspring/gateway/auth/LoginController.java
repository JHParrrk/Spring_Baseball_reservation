package com.firstspring.gateway.auth;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이 로그인 페이지 컨트롤러
 *
 * /login       : Google 로그인 버튼이 있는 HTML 페이지
 * /login/success : 로그인 성공 후 랜딩 페이지 (토큰은 HTTP-only 쿠키로 전달)
 *
 * OAuth2 흐름:
 * /oauth2/authorization/google → Google → /login/oauth2/code/google (게이트웨이 처리)
 * → JWT 쿠키 발급 → /login/success 리다이렉트
 */
@RestController
public class LoginController {

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> loginPage() {
        return Mono.just("""
                <!DOCTYPE html>
                <html>
                <head><title>Login</title></head>
                <body style="font-family:sans-serif; display:flex; justify-content:center; align-items:center; min-height:100vh; background:#f0f2f5;">
                    <div style="background:#fff; padding:40px; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,0.1); text-align:center;">
                        <h2>야구장 예약 시스템</h2>
                        <p style="color:#666;">Google 계정으로 로그인하면<br>JWT 토큰이 쿠키로 발급됩니다.</p>
                        <a href="/oauth2/authorization/google"
                           style="display:inline-block; padding:12px 24px; background:#4285f4; color:#fff;
                                  text-decoration:none; border-radius:6px; font-weight:bold; margin-top:16px;">
                            Login with Google
                        </a>
                    </div>
                </body>
                </html>
                """);
    }

    /**
     * 게이트웨이 단독 실행 시 fallback 페이지 (FE 없이 테스트할 때만 사용)
     *
     * APP_BASE_URL=http://localhost:3000 (FE 주소)으로 설정된 경우
     * OAuth2SuccessHandler가 FE의 /login/success 로 직접 리다이렉트하므로
     * 이 엔드포인트는 프론트엔드가 연결된 환경에서는 호출되지 않습니다.
     */
    @GetMapping(value = "/login/success", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> loginSuccess() {
        // JWT는 HTTP-only 쿠키(auth_token)로 저장되어 URL에 노출되지 않습니다.
        // 이후 API 요청 시 쿠키가 자동으로 첨부됩니다.
        return Mono.just("""
                <!DOCTYPE html>
                <html>
                <head><title>Login Success</title></head>
                <body style="font-family:sans-serif; padding:20px; background:#f4f4f4;">
                    <div style="background:#fff; padding:30px; border-radius:8px; max-width:500px;
                                margin:60px auto; box-shadow:0 2px 10px rgba(0,0,0,0.1);">
                        <h2 style="color:#27ae60;">로그인 성공</h2>
                        <p>JWT 토큰이 <code>auth_token</code> HTTP-only 쿠키로 저장되었습니다.</p>
                        <p style="color:#888; font-size:0.9em;">
                            이후 API 요청 시 브라우저가 쿠키를 자동으로 첨부합니다.<br>
                            Swagger 사용 시에는 쿠키가 자동으로 적용됩니다.
                        </p>
                    </div>
                </body>
                </html>
                """);
    }
}
