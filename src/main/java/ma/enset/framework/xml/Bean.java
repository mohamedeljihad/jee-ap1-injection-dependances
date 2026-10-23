package ma.enset.framework.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <bean id="..." class="...">
 *    <constructor-arg ref="..."/>   injection par constructeur
 *    <property name="..." ref="..."/>  injection par setter
 *    <field name="..." ref="..."/>     injection directe dans l'attribut
 * </bean>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Bean {
    @XmlAttribute(required = true)
    private String id;
    @XmlAttribute(name = "class", required = true)
    private String className;
    @XmlElement(name = "constructor-arg")
    private List<Dependency> constructorArgs = new ArrayList<>();
    @XmlElement(name = "property")
    private List<Dependency> properties = new ArrayList<>();
    @XmlElement(name = "field")
    private List<Dependency> fields = new ArrayList<>();

    public String getId() { return id; }
    public String getClassName() { return className; }
    public List<Dependency> getConstructorArgs() { return constructorArgs; }
    public List<Dependency> getProperties() { return properties; }
    public List<Dependency> getFields() { return fields; }
}
