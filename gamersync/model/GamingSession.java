package gamersync.model;

// CO1: Class with private fields, access specifiers, methods
// CO2: toString() overrides = polymorphism
public class GamingSession {
    private int    sessionId;
    private String startTime;
    private String endTime;
    private int    duration;
    private String gameName;
    private int    custId;
    private int    pcId;

    public GamingSession() {}

    public GamingSession(int sessionId, String startTime, String endTime,
                         int duration, String gameName, int custId, int pcId) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.duration  = duration;
        this.gameName  = gameName;
        this.custId    = custId;
        this.pcId      = pcId;
    }

    // Getters
    public int    getSessionId() { return sessionId; }
    public String getStartTime() { return startTime; }
    public String getEndTime()   { return endTime; }
    public int    getDuration()  { return duration; }
    public String getGameName()  { return gameName; }
    public int    getCustId()    { return custId; }
    public int    getPcId()      { return pcId; }

    // Setters
    public void setSessionId(int sessionId)    { this.sessionId = sessionId; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime)     { this.endTime = endTime; }
    public void setDuration(int duration)      { this.duration = duration; }
    public void setGameName(String gameName)   { this.gameName = gameName; }
    public void setCustId(int custId)          { this.custId = custId; }
    public void setPcId(int pcId)              { this.pcId = pcId; }

    @Override // CO2: Polymorphism - method overriding
    public String toString() {
        return String.format("| %-5d | %-18s | %-18s | %-5d | %-12s | %-6d | %-5d |",
            sessionId, startTime, endTime, duration, gameName, custId, pcId);
    }
}
