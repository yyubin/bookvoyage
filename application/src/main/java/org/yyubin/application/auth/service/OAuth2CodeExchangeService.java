package org.yyubin.application.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.application.auth.OAuth2CodeExchangeUseCase;
import org.yyubin.application.auth.port.OAuth2CodePort;
import org.yyubin.application.dto.OAuth2TokenResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2CodeExchangeService implements OAuth2CodeExchangeUseCase {

    private final OAuth2CodePort oAuth2CodePort;

    @Override
    public OAuth2TokenResult execute(String code) {
        String tokenData = oAuth2CodePort.getAndDeleteCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OAuth2 code"));

        String[] tokens = tokenData.split(":");
        if (tokens.length != 2) {
            throw new IllegalStateException("Invalid token data format");
        }

        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        log.info("OAuth2 code exchange success");

        return new OAuth2TokenResult(accessToken, refreshToken);
    }
}
