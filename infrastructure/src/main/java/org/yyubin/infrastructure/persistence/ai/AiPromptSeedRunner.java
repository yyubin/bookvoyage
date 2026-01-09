package org.yyubin.infrastructure.persistence.ai;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPromptSeedRunner implements ApplicationRunner {

    private final AiPromptJpaRepository promptRepository;
    private final AiPromptVersionJpaRepository versionRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedPromptIfMissing(
            "community_trend",
            "Community reading trend analysis prompt",
            defaultCommunityTrendTemplate()
        );
        seedPromptIfMissing(
            "user_analysis",
            "User preference analysis prompt",
            defaultUserAnalysisTemplate()
        );
        seedPromptIfMissing(
            "recommendation_explanation",
            "Recommendation explanation prompt",
            defaultRecommendationExplanationTemplate()
        );
    }

    private void seedPromptIfMissing(String key, String description, String template) {
        AiPromptEntity prompt = promptRepository.findByPromptKey(key)
            .orElseGet(() -> {
                LocalDateTime now = LocalDateTime.now();
                AiPromptEntity created = AiPromptEntity.builder()
                    .promptKey(key)
                    .description(description)
                    .active(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
                return promptRepository.save(created);
            });

        Optional<AiPromptVersionEntity> activeVersion =
            versionRepository.findFirstByPromptIdAndActiveTrueOrderByVersionDesc(prompt.getId());
        if (activeVersion.isPresent()) {
            return;
        }

        int nextVersion = versionRepository.findFirstByPromptIdOrderByVersionDesc(prompt.getId())
            .map(v -> v.getVersion() + 1)
            .orElse(1);

        AiPromptVersionEntity version = AiPromptVersionEntity.builder()
            .promptId(prompt.getId())
            .version(nextVersion)
            .template(template)
            .model("gpt-4o-mini")
            .temperature(0.7)
            .maxTokens(500)
            .provider("openai")
            .active(true)
            .createdBy("seed")
            .createdAt(LocalDateTime.now())
            .build();

        versionRepository.save(version);
        log.info("Seeded AI prompt version - key: {}, version: {}", key, nextVersion);
    }

    private String defaultCommunityTrendTemplate() {
        return """
            당신은 독서 커뮤니티 트렌드 분석 전문가입니다.

            최근 독서 커뮤니티의 전반적인 분위기와 경향성을 분석하여
            다음 JSON 형식으로 응답하세요:
            {
              "keywords": ["키워드1", "키워드2", "키워드3"],
              "summary": "현재 커뮤니티 분위기에 대한 한 문장 요약",
              "genres": [
                {
                  "genre": "장르명",
                  "percentage": 35.5,
                  "mood": "상승세"
                }
              ]
            }

            mood는 "상승세", "하락세", "안정" 중 하나입니다.
            """;
    }

    private String defaultUserAnalysisTemplate() {
        return """
            당신은 독서 취향 분석 전문가입니다.

            사용자 정보:
            - 사용자 ID: {userId}
            - 닉네임: {nickname}

            이 사용자의 독서 성향을 분석하고 다음 JSON 형식으로 응답하세요:
            {
              "persona_type": "성향을 나타내는 영문 키워드",
              "summary": "한 문장 요약",
              "keywords": ["키워드1", "키워드2", "키워드3"],
              "recommendations": [
                {
                  "book_title": "책 제목",
                  "author": "저자",
                  "reason": "추천 이유"
                }
              ]
            }
            """;
    }

    private String defaultRecommendationExplanationTemplate() {
        return """
            당신은 친절한 독서 추천 전문가입니다.

            사용자 정보:
            - 닉네임: {nickname}
            - 취향 태그: {tasteTag}

            추천하는 책:
            - 제목: {bookTitle}
            - 추천 점수: {scoreInfo}

            이 책을 추천하는 이유를 친절하고 자연스럽게 한두 문장으로 설명해주세요.
            마치 친구에게 책을 추천하는 것처럼 따뜻하고 구체적으로 작성하세요.
            """;
    }
}
