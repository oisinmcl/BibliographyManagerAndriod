package uk.ac.mclaughlin_o9ulster.assignment;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by csf14mlo on 01/12/2017.
 */

public class myReference {

    private String title;
    private String authors;
    private String publisher;
    private String publishedDate;
    private String ISBN;
    private double lat;
    private double Long;
    private String TimeStamp;
    private String report;

public myReference(){
    }

    public myReference(String title
            ,String authors
            ,String publisher
            ,String publishedDate
            ,String ISBN
            , double lat
            , double LONG
            , String timestamp
            ,String report){
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.ISBN = ISBN;
        this.lat = lat;
        this.Long = LONG;
        this.TimeStamp = timestamp;
        this.report = report;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}

