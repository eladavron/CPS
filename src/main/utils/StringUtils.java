package utils;

import entity.Subscription;

public class StringUtils {

    /**
     * Helps converts Enums of Subscription types to strings.
     * @param type
     * @return
     */
    public static String SubscriptionTypeName(Subscription.SubscriptionType type)
    {
        switch (type) {
            case FULL:
                return "Full";
            case REGULAR_MULTIPLE:
            case REGULAR:
                return "Regular";
            default:
                return null;
        }
    }

    /**
     * Sanitizes strings before insertion to SQL
     * @return The sanitized string
     */
    public static String sanitizeForSQL(String input)
    {
        return input.replaceAll("`", "").replaceAll("'","''");
    }

    /**
     * Desanitizes strings after SQL retrieval.
     * @param input
     * @return
     */
    public static String desanitizeFromSQL(String input)
    {
        return input.replaceAll("''", "'");
    }
}
