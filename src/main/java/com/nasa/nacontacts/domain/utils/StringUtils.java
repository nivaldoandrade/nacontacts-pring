package com.nasa.nacontacts.domain.utils;

import java.text.Normalizer;

public final class StringUtils {

    public static String removeAccents(String input) {
        return input == null
                ? null
                : Normalizer.normalize(input, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
    }
}
