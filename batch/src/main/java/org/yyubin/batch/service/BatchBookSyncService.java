package org.yyubin.batch.service;

import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;

/**
 * 배치 작업을 위한 북 동기화 서비스
 * Infrastructure Repository 직접 접근을 캡슐화
 */
public interface BatchBookSyncService {

    /**
     * BookEntity로부터 동기화용 DTO를 생성
     *
     * @param entity 북 엔티티
     * @return 동기화용 DTO (리뷰, 위시리스트, 키워드 등 통계 포함)
     */
    BookSyncDto buildSyncData(BookEntity entity);
}
