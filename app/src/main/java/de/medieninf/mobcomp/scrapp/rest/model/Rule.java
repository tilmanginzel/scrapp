package de.medieninf.mobcomp.scrapp.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for Rule.
 */
public class Rule {
    private String description;
    private Integer ruleId;
    private String title;
    private String updatedAtServer;
    private List<Action> actions;

    public Rule() {
        actions = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "Rule{" +
                ", description='" + description + '\'' +
                ", ruleId=" + ruleId +
                ", title='" + title + '\'' +
                ", updatedAtServer=" + updatedAtServer +
                ", actions=" + actions +
                '}';
    }

    public String getUpdatedAtServer() {
        return updatedAtServer;
    }

    public void setUpdatedAtServer(String updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }
}
