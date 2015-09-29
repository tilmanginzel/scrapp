package de.medieninf.mobcomp.scrapp.rest.model;

import java.util.Date;

/**
 * POJO for ActionParam.
 */
public class ActionParam {
    private Integer actionParamId;
    private String title;
    private String key;
    private String value;
    private String type;
    private Boolean required;
    private String updatedAtServer;

    public ActionParam() {
    }

    public ActionParam(Integer actionParamId, String title, String key, String value, String type, boolean required, Date createdAt, Date updatedAt) {
        this.actionParamId = actionParamId;
        this.title = title;
        this.key = key;
        this.value = value;
        this.type = type;
        this.required = required;
    }

    public Integer getActionParamId() {
        return actionParamId;
    }

    public void setActionParamId(Integer actionParamId) {
        this.actionParamId = actionParamId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ActionParam{" +
                "actionParamId=" + actionParamId +
                ", title='" + title + '\'' +
                ", key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", updatedAtServer=" + updatedAtServer +
                '}';
    }

    public String getUpdatedAtServer() {
        return updatedAtServer;
    }

    public void setUpdatedAtServer(String updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
