package qzui.domain;

public class TriggerHistory {

    private String triggerName;
    private String triggerGroup;
    private String prevFiredDate;
    private String prevFiredTime;
    private String nextFiredDate;
    private String nextFiredTime;
    private String firedDate;
    private String firedTime;
    private String ctxTriggerName;
    private String ctxTriggerGroup;
    private String refireCount;
    private String instructionCode;

    public String getTriggerName() {
        return this.triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerGroup() {
        return this.triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public String getPrevFiredDate() {
        return this.prevFiredDate;
    }

    public void setPrevFiredDate(String prevFiredDate) {
        this.prevFiredDate = prevFiredDate;
    }

    public String getPrevFiredTime() {
        return this.prevFiredTime;
    }

    public void setPrevFiredTime(String prevFiredTime) {
        this.prevFiredTime = prevFiredTime;
    }

    public String getNextFiredDate() {
        return this.nextFiredDate;
    }

    public void setNextFiredDate(String nextFiredDate) {
        this.nextFiredDate = nextFiredDate;
    }

    public String getNextFiredTime() {
        return this.nextFiredTime;
    }

    public void setNextFiredTime(String nextFiredTime) {
        this.nextFiredTime = nextFiredTime;
    }

    public String getFiredDate() {
        return this.firedDate;
    }

    public void setFiredDate(String firedDate) {
        this.firedDate = firedDate;
    }

    public String getFiredTime() {
        return this.firedTime;
    }

    public void setFiredTime(String firedTime) {
        this.firedTime = firedTime;
    }

    public String getCtxTriggerName() {
        return this.ctxTriggerName;
    }

    public void setCtxTriggerName(String ctxTriggerName) {
        this.ctxTriggerName = ctxTriggerName;
    }

    public String getCtxTriggerGroup() {
        return this.ctxTriggerGroup;
    }

    public void setCtxTriggerGroup(String ctxTriggerGroup) {
        this.ctxTriggerGroup = ctxTriggerGroup;
    }

    public String getRefireCount() {
        return this.refireCount;
    }

    public void setRefireCount(String refireCount) {
        this.refireCount = refireCount;
    }

    public String getInstructionCode() {
        return this.instructionCode;
    }

    public void setInstructionCode(String instructionCode) {
        this.instructionCode = instructionCode;
    }
}
