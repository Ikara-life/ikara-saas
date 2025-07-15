package studio.ikara.commons.util;

public class StringUtil {

    private StringUtil() {}

    public static String safeValueOf(Object obj, String... defaultValue) {

        if (obj == null) {
            for (String s : defaultValue) {
                if (s == null) continue;
                return s;
            }

            return null;
        }

        return obj.toString();
    }

    public static boolean safeIsBlank(Object object) {

        return object == null || object.toString().isBlank();
    }

    public static boolean safeEquals(String str, String str2) {
        return CommonsUtil.safeEquals(str, str2);
    }

    public static boolean onlyAlphabetAllowed(String appCode) {

        if (appCode == null) return false;

        for (int i = 0; i < appCode.length(); i++) {
            char c = appCode.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) continue;

            return false;
        }

        return true;
    }

    public static String removeLineFeedOrNewLineChars(String str) {

        if (str == null) return str;

        int len = str.length() - 1;

        while (len >= 0 && (str.charAt(len) == '\r' || str.charAt(len) == '\n')) len--;

        return str.substring(0, len + 1);
    }

    public static String removeSpecialCharacters(String str) {

        if (str == null || str.isBlank()) return str;

        StringBuilder sb = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLetter(c)) sb.append(c);
        }

        return sb.toString();
    }

    public static String trimToSize(Object obj, int size) {
        if (obj == null) return null;

        String str = obj.toString();

        if (str.length() <= size) return str;

        if (size > 10) return str.substring(0, size - 3) + "...";

        return str.substring(0, size);
    }

    public static String toTitleCase(String str) {
        if (str == null || str.isEmpty()) return null;

        char[] chars = str.trim().toCharArray();
        boolean newWord = true;

        for (int i = 0; i < chars.length; i++) {
            if (!Character.isSpaceChar(chars[i])) {
                newWord = true;
            } else {
                if (newWord) {
                    chars[i] = Character.toUpperCase(chars[i]);
                    newWord = false;
                } else {
                    chars[i] = Character.toLowerCase(chars[i]);
                }
            }
        }
        return new String(chars);
    }

    public static String toUpperCaseWithUnderscores(String str) {
        if (str == null || str.isBlank()) return str;
        return str.trim().replaceAll("\\s+", "_").toUpperCase();
    }
}
