package ma.enset.framework.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/** Racine du fichier de configuration XML (mapping objet/XML avec JAXB). */
@XmlRootElement(name = "beans")
@XmlAccessorType(XmlAccessType.FIELD)
public class Beans {
    @XmlElement(name = "bean")
    private List<Bean> beans = new ArrayList<>();

    public List<Bean> getBeans() { return beans; }
}
