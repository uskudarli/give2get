package models;

import com.boun.give2get.core.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 9, 2011
 * Time: 1:26:59 AM
 * To change this template use File | Settings | File Templates.
 */
public enum ActivityType {

    NEW_SERVICE, NEW_COMMENT, NEW_USER;

    public static ActivityType fromString(String statusAsString) {

        if (statusAsString.equalsIgnoreCase(ActivityType.NEW_SERVICE.toString()))
            return ActivityType.NEW_SERVICE;
        else if (statusAsString.equalsIgnoreCase(ActivityType.NEW_COMMENT.toString()))
            return ActivityType.NEW_COMMENT;
        else
            return ActivityType.NEW_USER;

    }


    public static  String getText(ActivityType type, String username) {

        String raw = getRawTextBody(type);

        raw = String.format(raw, new String[] {username});

        return raw;

    }

    public static final String getText(ActivityType type, String username, String serviceName) {

        String raw = getRawTextBody(type);

        raw = String.format(raw, new String[] {username, serviceName});        

        return raw;

    }


    private static final String getRawTextBody(ActivityType type) {

        if (type.equals(ActivityType.NEW_USER)) {

            return Messages.get("act.newuser");

        } else if (type.equals(ActivityType.NEW_COMMENT)) {

            return Messages.get("act.newcomment");

        } else {

            return Messages.get("act.newservice");
        }
    }

}
