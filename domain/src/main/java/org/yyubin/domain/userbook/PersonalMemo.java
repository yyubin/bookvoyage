package org.yyubin.domain.userbook;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonalMemo {
    private static final int MAX_LENGTH = 2000;
    private final String content;

    public static PersonalMemo of(String content) {
        if (content != null && content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Memo cannot exceed 2000 characters");
        }
        return new PersonalMemo(content);
    }

    public static PersonalMemo empty() {
        return new PersonalMemo(null);
    }

    public boolean hasContent() {
        return content != null && !content.isBlank();
    }

    public String getContent() {
        return content;
    }
}
