package org.yyubin.application.user.port;

import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;

public interface ProfileImageStoragePort {
    ProfileImageUploadUrlResult generateUploadUrl(String originalFilename);
}
