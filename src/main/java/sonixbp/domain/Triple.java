package sonixbp.domain;


public class Triple {

    public Triple(String subject, String predicate, String object, String type, String visibility) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.type = type;
        this.visibility = visibility;
    }

    String subject;
    String predicate;
    String object;

    String type;
    String visibility;

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public String getType() {
        return type;
    }

    public String getVisibility() {
        return visibility;
    }
}
