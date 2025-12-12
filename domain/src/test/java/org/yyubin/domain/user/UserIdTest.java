package org.yyubin.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserIdTest {

    @Test
    void validUserId() {
        UserId id = new UserId(1L);
        assertEquals(1L, id.value());
    }

    @Test
    void invalidUserIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(0L));
        assertThrows(IllegalArgumentException.class, () -> new UserId(null));
    }
}
