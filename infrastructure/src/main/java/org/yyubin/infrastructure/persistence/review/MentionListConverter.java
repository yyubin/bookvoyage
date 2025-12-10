package org.yyubin.infrastructure.persistence.review;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.yyubin.domain.review.Mention;

@Converter
public class MentionListConverter implements AttributeConverter<List<Mention>, String> {

    private static final String ITEM_DELIMITER = ";";
    private static final String FIELD_DELIMITER = ":";

    @Override
    public String convertToDatabaseColumn(List<Mention> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream()
                .map(m -> m.mentionedUserId() + FIELD_DELIMITER + m.startIndex() + FIELD_DELIMITER + m.endIndex())
                .collect(Collectors.joining(ITEM_DELIMITER));
    }

    @Override
    public List<Mention> convertToEntityAttribute(String dbData) {
        List<Mention> result = new ArrayList<>();
        if (dbData == null || dbData.isBlank()) {
            return result;
        }
        String[] items = dbData.split(ITEM_DELIMITER);
        for (String item : items) {
            String[] parts = item.split(FIELD_DELIMITER);
            if (parts.length == 3) {
                try {
                    Long userId = Long.parseLong(parts[0]);
                    int start = Integer.parseInt(parts[1]);
                    int end = Integer.parseInt(parts[2]);
                    result.add(new Mention(userId, start, end));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }
}
