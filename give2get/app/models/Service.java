package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas,orcunguducu
 * Date: Nov 13, 2011
 * Time: 1:28:39 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Service extends ListableModel{


    //public Service(String title, String description, String startDate, String endDate, int sehirId, int ilceId, int semtId,
      //             String fromDay, String toDay, String fromTime, String toTime) {
    
    private String title;
    private String description;
    private String startDate;
    private String endDate;

    private String sehirName;
    private int sehirId;

    private String ilceName;
    private int ilceId;

    private String semtName;
    private int semtId;

    private String fromDay;
    private String toDay;
    private String fromTime;
    private String toTime;


        
    private String created;
    private String status;

    private int requestCount;

    private int viewCount;

    private int providerId;
    private String providerFullName;
    private String providerEmail;

    private int requesterId;
    private String requesterFullName;
    private String requesterEmail;

    private List<Comment> comments;

    //  Advancded Search Results
    private int providerRating;


    public static Service newOnRollService(ResultSet rs, int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.requesterFullName   = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));
        service.requesterId         = rs.getInt("requester_id");
        service.status              = rs.getString("status");
        service.providerId          = rs.getInt("provider_id");

        service.oddOrEven           = index %2 == 0 ? "odd" :"even";

        return service;

    }


    public static Service getService(ResultSet rs,int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("SERVICE_ID");
        service.title               = rs.getString("title");
        service.description         = rs.getString("description");



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

    public Service(String title, String description, String startDate, String endDate, int sehirId, int ilceId, int semtId,
                   String fromDay, String toDay, String fromTime, String toTime) {

        this.title              = title;
        this.description        = description;
        this.startDate          = startDate;
        this.endDate            = endDate;
        this.sehirId            = sehirId;
        this.ilceId             = ilceId;
        this.semtId             = semtId;
        this.fromDay            = fromDay;
        this.toDay              = toDay;
        this.fromTime           = fromTime;
        this.toTime             = toTime;

    }

    public Service(String title, String description, String startDate, String endDate, String duration, String location, String period) {

        this.title          = title;
        this.description    = description;
        this.startDate      = startDate;
        this.endDate        = endDate;


    }

    public static Service newAdvancedSearched(ResultSet rs, int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("service_id");
        service.title               = rs.getString("title");
        service.description         = rs.getString("description");
        service.created             = rs.getString("created");
        service.viewCount           = rs.getInt("viewCount");
        service.providerId          = rs.getInt("PROVIDER_ID");
        service.providerRating      = rs.getInt("rating");
        service.providerFullName    = rs.getString("firstname") + " " + rs.getString("lastname");        

        service.oddOrEven   = index %2 == 0 ? "odd" :"even";

        return service;

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

        service.id                  = rs.getInt("SERVICE_ID");
        service.title               = rs.getString("title");
        service.description         = rs.getString("description");

        service.sehirName           = rs.getString("SEHIR");
        service.sehirName = (service.sehirName == null || service.sehirName.length() == 0) ? "-" : service.sehirName;

        service.ilceName            = rs.getString("ILCE");
        service.ilceName = (service.ilceName== null || service.ilceName.length() == 0) ? "-" : service.ilceName;

        service.semtName            = rs.getString("SEMT");
        service.semtName = (service.semtName == null || service.semtName.length() == 0) ? "-" : service.semtName;

        service.fromDay             = rs.getString("fromDay");
        //service.fromDay = (service.fromDay == null || service.fromDay.length() == 0) ? "-" : service.fromDay;

        service.toDay               = rs.getString("toDay");
        //service.toDay = (service.toDay == null || service.toDay.length() == 0) ? "-" : service.toDay;

        service.fromTime            = rs.getString("fromTime");
        //service.fromTime = (service.fromTime == null || service.fromTime.length() == 0) ? "" : service.fromTime;

        service.toTime              = rs.getString("toTime");
        //service.toTime = (service.toTime == null || service.toTime.length() ==0) ? "" : service.toTime;

        service.startDate           = rs.getString("start");

        service.startDate  = (service.startDate == null || service.startDate.length() == 0) ? "-" : service.startDate;

        service.endDate             = rs.getString("end");
        
        service.endDate = (service.endDate == null || service.endDate.length() == 0) ? "-" : service.endDate;


        service.created             = rs.getString("created");
        service.status              = rs.getString("status");
        service.startDate           = rs.getString("start");
        service.endDate             = rs.getString("end");
        service.viewCount           = rs.getInt("viewCount");

        service.providerId          = rs.getInt("provider_id");
        service.providerFullName    = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));

        service.requesterId         = rs.getInt("requester_id");

        
        return service;

    }

    public static Service newRequestedService(ResultSet rs, int index) throws SQLException {

        Service service = new Service();

        service.id                  = rs.getInt("id");
        service.title               = rs.getString("title");
        service.providerId          = rs.getInt("provider_id");
        service.providerFullName    = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));
        service.created             = rs.getString("requested_at");
        service.status              = rs.getString("status");

        service.oddOrEven   = index %2 == 0 ? "odd" :"even";

        return service;

    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getRequestCount() {
        return requestCount;
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

 

    public String getProviderEmail() {
        return providerEmail;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public String getSehirName() {
        return sehirName;
    }

    public int getSehirId() {
        return sehirId;
    }

    public String getIlceName() {
        return ilceName;
    }

    public int getIlceId() {
        return ilceId;
    }

    public String getSemtName() {
        return semtName;
    }

    public int getSemtId() {
        return semtId;
    }

    public String getFromDay() {
        return fromDay;
    }

    public String getToDay() {
        return toDay;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    @Override
    public String toString() {
        return "Service{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", sehirName='" + sehirName + '\'' +
                ", sehirId=" + sehirId +
                ", ilceName='" + ilceName + '\'' +
                ", ilceId=" + ilceId +
                ", semtName='" + semtName + '\'' +
                ", semtId=" + semtId +
                ", fromDay='" + fromDay + '\'' +
                ", toDay='" + toDay + '\'' +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                ", created='" + created + '\'' +
                ", status='" + status + '\'' +
                ", requestCount=" + requestCount +
                ", viewCount=" + viewCount +
                ", providerId=" + providerId +
                ", providerFullName='" + providerFullName + '\'' +
                ", providerEmail='" + providerEmail + '\'' +
                ", requesterId=" + requesterId +
                ", requesterFullName='" + requesterFullName + '\'' +
                ", requesterEmail='" + requesterEmail + '\'' +
                ", comments=" + comments +
                '}';
    }
}
