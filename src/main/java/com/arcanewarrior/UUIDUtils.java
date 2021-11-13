package com.arcanewarrior;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UUIDUtils {

    public static String stripDashesFromUUID(@NotNull UUID uid) {
        return uid.toString().replaceAll("-", "");
    }

    // Much thanks https://stackoverflow.com/questions/18986712/creating-a-uuid-from-a-string-with-no-dashes
    public static UUID makeUUIDFromStringWithoutDashes(@NotNull String id) {
        return UUID.fromString(id
                .replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                ));
    }
}
