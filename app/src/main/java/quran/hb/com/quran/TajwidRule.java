package quran.hb.com.quran;

/**
 * Modèle représentant une règle de Tajwid détectée lors de l'analyse d'une récitation.
 *
 * severity : 0 = correct (OK), 1 = avertissement, 2 = erreur
 */
public class TajwidRule {

    public static final int SEVERITY_OK      = 0;
    public static final int SEVERITY_WARNING = 1;
    public static final int SEVERITY_ERROR   = 2;

    /** Nom de la règle en arabe (ex : "إظهار") */
    private String ruleNameAr;

    /** Nom de la règle en français (ex : "Izhar – prononciation distincte") */
    private String ruleNameFr;

    /** Description de la règle / explication pédagogique */
    private String description;

    /** Position (index de caractère) dans le texte reconnu où la règle s'applique */
    private int position;

    /** Niveau de gravité : SEVERITY_OK(0), SEVERITY_WARNING(1), SEVERITY_ERROR(2) */
    private int severity;

    /** Texte prononcé incorrectement (peut être null si severity == OK) */
    private String wrongText;

    /** Texte de référence correct correspondant (peut être null si severity == OK) */
    private String correctText;

    // ------------------------------------------------------------------ //
    //  Constructeurs
    // ------------------------------------------------------------------ //

    public TajwidRule() {
    }

    public TajwidRule(String ruleNameAr, String ruleNameFr, String description,
                      int position, int severity, String wrongText, String correctText) {
        this.ruleNameAr  = ruleNameAr;
        this.ruleNameFr  = ruleNameFr;
        this.description = description;
        this.position    = position;
        this.severity    = severity;
        this.wrongText   = wrongText;
        this.correctText = correctText;
    }

    // ------------------------------------------------------------------ //
    //  Getters / Setters
    // ------------------------------------------------------------------ //

    public String getRuleNameAr()  { return ruleNameAr;  }
    public void   setRuleNameAr(String ruleNameAr)  { this.ruleNameAr  = ruleNameAr;  }

    public String getRuleNameFr()  { return ruleNameFr;  }
    public void   setRuleNameFr(String ruleNameFr)  { this.ruleNameFr  = ruleNameFr;  }

    public String getDescription() { return description; }
    public void   setDescription(String description) { this.description = description; }

    public int    getPosition()    { return position;    }
    public void   setPosition(int position) { this.position = position; }

    public int    getSeverity()    { return severity;    }
    public void   setSeverity(int severity) { this.severity = severity; }

    public String getWrongText()   { return wrongText;   }
    public void   setWrongText(String wrongText) { this.wrongText = wrongText; }

    public String getCorrectText() { return correctText; }
    public void   setCorrectText(String correctText) { this.correctText = correctText; }

    // ------------------------------------------------------------------ //
    //  Utilitaire
    // ------------------------------------------------------------------ //

    /** Retourne l'icône textuelle correspondant au niveau de sévérité. */
    public String getSeverityIcon() {
        switch (severity) {
            case SEVERITY_OK:      return "✅";
            case SEVERITY_WARNING: return "⚠️";
            case SEVERITY_ERROR:   return "❌";
            default:               return "ℹ️";
        }
    }

    @Override
    public String toString() {
        return getSeverityIcon() + " " + ruleNameAr + " (" + ruleNameFr + ")"
                + " pos=" + position
                + (wrongText != null ? " [" + wrongText + " → " + correctText + "]" : "");
    }
}
