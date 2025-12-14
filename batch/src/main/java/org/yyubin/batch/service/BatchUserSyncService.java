package org.yyubin.batch.service;

import org.yyubin.batch.sync.UserSyncDto;
import org.yyubin.infrastructure.persistence.user.UserEntity;

/**
 * 배치 작업을 위한 유저 동기화 서비스
 * Infrastructure Repository 직접 접근을 캡슐화
 */
public interface BatchUserSyncService {

    /**
     * UserEntity로부터 동기화용 DTO를 생성
     *
     * @param entity 유저 엔티티
     * @return 동기화용 DTO (조회 기록, 위시리스트, 좋아요한 리뷰 등 포함)
     */
    UserSyncDto buildSyncData(UserEntity entity);
}
