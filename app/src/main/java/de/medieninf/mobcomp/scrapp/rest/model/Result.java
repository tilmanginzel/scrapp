package de.medieninf.mobcomp.scrapp.rest.model;

import java.util.Date;

/**
 * POJO for Result.
 */
public class Result {
    private int resultId;
    private String content;
    private String hash;
    private Date lastModified;

    public Result() {
    }

    public Result(Result r) {
        this.resultId = r.resultId;
        this.content = r.content;
        this.hash = r.hash;
        this.lastModified = r.lastModified;
    }

    public int getResultId() { return resultId; }

    public void setResultId(int id) { this.resultId = id; }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
