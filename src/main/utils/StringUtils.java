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
}
