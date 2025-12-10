package org.yyubin.support.nickname;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class NicknameGenerator {

    private static final String[] ADJECTIVES = {
            "silent","gentle","soft","subtle","lucid","mellow",
            "shadow","lunar","astral","cosmic","nebula",
            "crystal","echoing","fading","distant","hidden",
            "quiet","fleeting","misty","twilight","rising",
            "drifting","glowing","silver","golden","warm",
            "tranquil","still","pale","whispering",
            "radiant","frozen","wandering",
            "serene","boundless","hollow","airy"
    };

    private static final String[] NOUNS = {
            "breeze","echo","nova","river","forest","valley",
            "harbor","meadow","dune","horizon","orchard",
            "mist","frost","ember","lantern","beacon",
            "cloud","stream","tide","star","moon",
            "archive","pulse","whisper",
            "fragment","circuit","prism","surface",
            "shelter","field","wave","current","drift",
            "crest","station","pattern","spectrum","portal"
    };

    public static String generate(String seed) {
        byte[] hash = sha256(seed);

        // 해시 일부를 바로 index로 사용
        int adjIndex = unsignedByte(hash[0]) % ADJECTIVES.length;
        int nounIndex = unsignedByte(hash[1]) % NOUNS.length;

        // 숫자 생성 (0~9999)
        int number = ((unsignedByte(hash[2]) << 8) | unsignedByte(hash[3])) % 10_000;

        return ADJECTIVES[adjIndex] + "-" + NOUNS[nounIndex] + "-" + String.format("%04d", number);
    }

    private static int unsignedByte(byte b) {
        return b & 0xFF;
    }

    private static byte[] sha256(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(seed.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private NicknameGenerator() {}
}
