package org.yyubin.application.user.command;

public record UpdateUserNicknameCommand(
        Long userId,
        String newNickname
) {
    public UpdateUserNicknameCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (newNickname == null) {
            throw new IllegalArgumentException("NewNickName cannot be null");
        }
        if (newNickname.length() > 30) {
            throw new IllegalArgumentException("NewNickName must not exceed 30 characters");
        }
    }
}
