package com.akapps.dailynote.classes.helpers;

import java.security.SecureRandom;

public class UniqueIDGenerator {

    private static final String CHARACTERS = "0123456789";
    private static final int ID_LENGTH = 9;

    public static int generateUniqueID() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return Integer.parseInt(sb.toString());
    }
}
