package org.yyubin.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yyubin.application.user.command.UpdateUserBioCommand;
import org.yyubin.application.user.command.UpdateUserNicknameCommand;
import org.yyubin.application.user.command.UpdateUserProfileImageUrlCommand;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.user.port.UpdateUserPort;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;


@Service
@RequiredArgsConstructor
public class UpdateUserCommandHandler {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;

    public void handle(UpdateUserBioCommand command) {
        User user = loadUserPort.loadById(new UserId(command.userId()));

        User updatedUser = user.updateProfile(user.username(), command.bio(), user.nickname(), user.ProfileImageUrl());

        updateUserPort.update(updatedUser);
    }

    public void handle(UpdateUserNicknameCommand command) {
        User user = loadUserPort.loadById(new UserId(command.userId()));

        User updatedUser = user.updateProfile(user.username(), user.bio(), command.newNickname(), user.ProfileImageUrl());

        updateUserPort.update(updatedUser);
    }

    public void handle(UpdateUserProfileImageUrlCommand command) {
        User user = loadUserPort.loadById(new UserId(command.userId()));

        User updatedUser = user.updateProfile(user.username(), user.bio(), user.nickname(), command.newProfileImageUrl());

        updateUserPort.update(updatedUser);
    }
}
