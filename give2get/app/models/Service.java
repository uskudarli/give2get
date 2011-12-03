package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 13, 2011
 * Time: 1:28:39 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Service extends ListableModel{
    
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String duration;
    private String location;
    private String period;
    private String created;
    private String status;

    private int viewCount;

    private int providerId;
    private String providerFullName;

    private List<Comment> comments;


    public static Service getService(ResultSet rs,int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.description         = rs.getString("description");
        service.location            = rs.getString("location");
        service.period              = rs.getString("period");
        service.duration            = rs.getString("duration");
        service.created             = rs.getString("created");
        service.status              = rs.getString("status");
        service.startDate           = rs.getString("start");
        service.endDate             = rs.getString("end");
        service.viewCount           = rs.getInt("viewCount");

        service.providerId          = rs.getInt("provider_id");
        service.providerFullName    = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));

        service.oddOrEven   = index %2 == 0 ? "odd" :"even";
        
        return service;

    }
    
    public Service() {        
    }

    public Service(String title, String description, String startDate, String endDate, String duration, String location, String period) {

        this.title          = title;
        this.description    = description;
        this.startDate      = startDate;
        this.endDate        = endDate;
        this.duration       = duration;
        this.location       = location;
        this.period         = period;

    }

    public static Service newMyService(ResultSet rs, int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.created             = rs.getString("created");
        service.status              = rs.getString("status");

        service.oddOrEven   = index %2 == 0 ? "odd" :"even";

        return service;

    }


    public static Service newProvidedService(ResultSet rs, int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.created             = rs.getString("created");
        service.status              = rs.getString("status");
        service.providerId          = rs.getInt("provider_id");
        service.providerFullName    = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));

        service.oddOrEven   = index %2 == 0 ? "odd" :"even";

        return service;

    }

    public static Service newDetailedService(ResultSet rs) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.description         = rs.getString("description");
        service.location            = rs.getString("location");
        service.period              = rs.getString("period");
        service.duration            = rs.getString("duration");
        service.created             = rs.getString("created");
        service.status              = rs.getString("status");
        service.startDate           = rs.getString("start");
        service.endDate             = rs.getString("end");
        service.viewCount           = rs.getInt("viewCount");

        service.providerId          = rs.getInt("provider_id");
        service.providerFullName    = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));

        
        return service;

    }
    
    
    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderFullName() {
        return providerFullName;
    }

    public void setProviderFullName(String providerFullName) {
        this.providerFullName = providerFullName;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getCreated() {
        return created;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public String getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return "Service{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", duration='" + duration + '\'' +
                ", location='" + location + '\'' +
                ", period='" + period + '\'' +
                '}';
    }
}
