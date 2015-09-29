package de.medieninf.mobcomp.scrapp.rest.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * POJO for Action.
 */
public class Action {
    private Integer actionId;
    private String title;
    private Integer position;
    private String method;
    private String url;
    private String parseExpression;
    private String parseType;
    private String parseExpressionDisplay;
    private String parseTypeDisplay;
    private String updatedAtServer;
    private List<ActionParam> actionParams;

    public Action() {
        actionParams = new ArrayList<>();
    }

    public Action(Integer actionId, String title, Integer position, String method, String url, String parseExpression, String parseType, String parseExpressionDisplay, String parseTypeDisplay, Date createdAt, Date updatedAt, List<ActionParam> actionParams) {
        this.actionId = actionId;
        this.title = title;
        this.position = position;
        this.method = method;
        this.url = url;
        this.parseExpression = parseExpression;
        this.parseType = parseType;
        this.parseExpressionDisplay = parseExpressionDisplay;
        this.parseTypeDisplay = parseTypeDisplay;
        this.actionParams = actionParams;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParseExpression() {
        return parseExpression;
    }

    public void setParseExpression(String parseExpression) {
        this.parseExpression = parseExpression;
    }

    public String getParseType() {
        return parseType;
    }

    public void setParseType(String parseType) {
        this.parseType = parseType;
    }

    public List<ActionParam> getActionParams() {
        return actionParams;
    }

    public void setActionParams(List<ActionParam> actionParams) {
        this.actionParams = actionParams;
    }

    public String getParseExpressionDisplay() {
        return parseExpressionDisplay;
    }

    public void setParseExpressionDisplay(String parseExpressionDisplay) {
        this.parseExpressionDisplay = parseExpressionDisplay;
    }

    public String getParseTypeDisplay() {
        return parseTypeDisplay;
    }

    public void setParseTypeDisplay(String parseTypeDisplay) {
        this.parseTypeDisplay = parseTypeDisplay;
    }

    @Override
    public String toString() {
        return "Action{" +
                "actionId=" + actionId +
                ", title='" + title + '\'' +
                ", position=" + position +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", parseExpression='" + parseExpression + '\'' +
                ", parseType='" + parseType + '\'' +
                ", parseExpressionDisplay='" + parseExpressionDisplay + '\'' +
                ", parseTypeDisplay='" + parseTypeDisplay + '\'' +
                ", updatedAtServer='" + updatedAtServer + '\'' +
                ", actionParams=" + actionParams +
                '}';
    }

    public String getUpdatedAtServer() {
        return updatedAtServer;
    }

    public void setUpdatedAtServer(String updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }
}
