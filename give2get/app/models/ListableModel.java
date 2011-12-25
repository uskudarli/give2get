package models;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 13, 2011
 * Time: 11:48:04 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ListableModel {

    protected int id;
    protected String oddOrEven;


    public int getId() {
        return id;
    }

    public String getOddOrEven() {
        return oddOrEven;
    }

    @Override
    public String toString() {
        return "ListableModel{" +
                "id=" + id +
                ", oddOrEven='" + oddOrEven + '\'' +
                '}';
    }
}
