package models;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 5, 2011
 * Time: 12:11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ServiceProgress {

    private int id;

    private String code;
    private int serviceRequestId;

    private int providerRatingId;
    private int consumerRatingId;

    private String status;
    private String created;

    public static ServiceProgress createNew(int servicRequestId) {

        ServiceProgress serviceProgres = new ServiceProgress();

        serviceProgres.code                 = UUID.randomUUID().toString();
        serviceProgres.serviceRequestId     = servicRequestId;
        serviceProgres.status               = ServiceProgressStatus.NOT_RESOLVED.name();

        return serviceProgres;

    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getServiceRequestId() {
        return serviceRequestId;
    }

    public int getProviderRatingId() {
        return providerRatingId;
    }

    public int getConsumerRatingId() {
        return consumerRatingId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }
}
