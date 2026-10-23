package ma.enset.framework.xml;

import javax.xml.bind.annotation.*;

/** Une dépendance : référence vers un autre bean (ref) ou valeur littérale (value). */
@XmlAccessorType(XmlAccessType.FIELD)
public class Dependency {
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String ref;
    @XmlAttribute
    private String value;

    public String getName() { return name; }
    public String getRef() { return ref; }
    public String getValue() { return value; }
}
