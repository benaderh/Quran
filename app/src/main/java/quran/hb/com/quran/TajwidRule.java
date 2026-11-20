package quran.hb.com.quran;

public class TajwidRule {
    public static final int SEVERITY_OK      = 0;
    public static final int SEVERITY_WARNING = 1;
    public static final int SEVERITY_ERROR   = 2;

    private String ruleNameAr;
    private String ruleNameFr;
    private String description;
    private int    severity;
    private String affectedText;

    public TajwidRule(String ruleNameAr, String ruleNameFr, String description,
                      int severity, String affectedText) {
        this.ruleNameAr   = ruleNameAr;
        this.ruleNameFr   = ruleNameFr;
        this.description  = description;
        this.severity     = severity;
        this.affectedText = affectedText;
    }

    public String getRuleNameAr()  { return ruleNameAr; }
    public String getRuleNameFr()  { return ruleNameFr; }
    public String getDescription() { return description; }
    public int    getSeverity()    { return severity; }
    public String getAffectedText(){ return affectedText; }

    public String getSeverityIcon() {
        switch (severity) {
            case SEVERITY_ERROR:   return "❌";
            case SEVERITY_WARNING: return "⚠️";
            default:               return "✅";
        }
    }
}