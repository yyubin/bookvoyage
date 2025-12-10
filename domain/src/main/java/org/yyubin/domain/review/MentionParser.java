package org.yyubin.domain.review;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MentionParser {

    private static final Pattern PATTERN = Pattern.compile("@([A-Za-z0-9_]+)");

    private final UserFinder userFinder;

    public List<Mention> parse(String content) {
        List<Mention> results = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return results;
        }

        Matcher matcher = PATTERN.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            Long userId = userFinder.findUserIdByUsername(username);
            if (userId != null) {
                results.add(new Mention(
                        userId,
                        matcher.start(),
                        matcher.end()
                ));
            }
        }

        return results;
    }
}
