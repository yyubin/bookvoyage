package org.yyubin.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LogoutCleanupHandler {
    void clear(HttpServletRequest request, HttpServletResponse response);
}
