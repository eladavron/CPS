package utils;

import entity.Subscription;

public class StringUtils {

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
        return input.replaceAll("'", "''");
    }

    public static String desanitizeFromSQL(String input)
    {
        return input.replaceAll("''", "'");
    }
}
