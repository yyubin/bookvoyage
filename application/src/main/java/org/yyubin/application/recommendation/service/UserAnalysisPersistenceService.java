package org.yyubin.application.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.domain.ai.AiUserAnalysisRecord;

@Service
@RequiredArgsConstructor
public class UserAnalysisPersistenceService {

    private final AiUserAnalysisPort analysisPort;

    @Transactional
    public void save(AiUserAnalysisRecord record) {
        analysisPort.save(record);
    }
}
