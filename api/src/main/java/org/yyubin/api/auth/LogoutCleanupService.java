package org.yyubin.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.yyubin.support.web.CookieProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCleanupService implements LogoutCleanupHandler {

    private final CookieProperties cookieProperties;

    @Override
    public void clear(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.debug("Server session invalidated");
        }

        deleteCookie(response, "JSESSIONID");
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(0);

        if (cookieProperties.getSameSite() != null && !cookieProperties.getSameSite().isBlank()) {
            builder.sameSite(cookieProperties.getSameSite());
        }
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }
}
