package org.yyubin.application.profile;

import org.yyubin.application.profile.dto.ProfileSummaryResult;
import org.yyubin.application.profile.query.GetProfileSummaryQuery;

public interface GetProfileSummaryUseCase {
    ProfileSummaryResult query(GetProfileSummaryQuery query);
}
