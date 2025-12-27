package org.yyubin.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;
import org.yyubin.application.user.port.ProfileImageStoragePort;

@Service
@RequiredArgsConstructor
public class GenerateProfileImageUploadUrlQueryHandler {

    private final ProfileImageStoragePort profileImageStoragePort;

    public ProfileImageUploadUrlResult handle(GenerateProfileImageUploadUrlQuery query) {
        return profileImageStoragePort.generateUploadUrl(query.filename());
    }
}
