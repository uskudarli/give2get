package models;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 4, 2011
 * Time: 11:49:21 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ServiceStatus {

    WAITING_FOR_REQUESTS ("Waiting for requests"),
    WAITING_TO_BE_RESOLVED ("Resolving"),
    RESOLVED ("Resolved");

    private final String name;

    ServiceStatus(String name) {
        this.name =  name;
    }

    public String getName() {
        return name;
    }
}
