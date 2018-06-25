package uk.ac.mclaughlin_o9ulster.assignment;

/**
 * Created by Oisin on 09/12/2017.
 */




public class myReport {
    private String title;
    private String module;
    private String submissionDate;
    private Boolean reminder;

    public myReport(){


    }

    public myReport(String title,String module,String submissionDate,Boolean reminder){
        this.title = title;
        this.module = module;
        this.submissionDate = submissionDate;
        this.reminder = reminder;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Boolean getReminder() {
        return reminder;
    }

    public void setReminder(Boolean reminder) {
        this.reminder = reminder;
    }
}
