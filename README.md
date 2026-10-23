# Activité Pratique N°1 : Injection des dépendances

**Mohammed ELJIHAD**, Master II-BDCC, ENSET Mohammedia. Module : Architecture JEE et Systèmes Distribués (Pr. Mohamed YOUSSFI).

## Objectif
Comprendre le **couplage faible** et l'**inversion de contrôle**. Le code métier dépend d'interfaces ; les implémentations concrètes sont injectées de l'extérieur :
- par instanciation statique ;
- par instanciation dynamique (réflexion) ;
- avec Spring, en version XML et en version annotations ;
- avec un **mini framework IOC** écrit dans le projet (partie 2).

## Structure du projet
```
src/main/java/ma/enset
├── dao/            IDao (getData) + DaoImpl (version base de données)
├── ext/            DaoImplV2 (version capteurs) : extension sans modifier le code existant
├── metier/         IMetier (calcul) + MetierImpl (couplage faible : ne connaît que IDao)
├── presentation/   PresStatique, PresDynamique, PresSpringXML, PresSpringAnnotation
├── framework/      Mini framework IOC (partie 2)
│   ├── annotations/   @Component, @Inject, @Qualifier
│   ├── xml/           Beans, Bean, Dependency (mapping objet/XML avec JAXB)
│   └── context/       ApplicationContext, XmlApplicationContext, AnnotationApplicationContext
└── demo/           Composants d'exemple utilisant le mini framework
src/main/resources
├── applicationContext.xml   configuration Spring XML
└── mini-beans.xml           configuration XML du mini framework
config.txt                   noms des classes pour l'instanciation dynamique
```

## Partie 1 : injection des dépendances

### 1-2. Interface IDao et son implémentation
```java
public interface IDao { double getData(); }

@Component("dao")
public class DaoImpl implements IDao {
    public double getData() { System.out.println("Version base de données"); return 34; }
}
```

### 3-4. Interface IMetier et implémentation avec couplage faible
`MetierImpl` n'utilise que l'interface `IDao`, sans jamais faire `new DaoImpl()`. On peut donc changer de DAO (`DaoImplV2`) sans modifier ni recompiler la couche métier : l'application est **fermée à la modification et ouverte à l'extension**.
```java
@Component("metier")
public class MetierImpl implements IMetier {
    private IDao dao;
    public MetierImpl() { }
    @Autowired
    public MetierImpl(@Qualifier("dao") IDao dao) { this.dao = dao; }    // injection par constructeur
    public double calcul() { double t = dao.getData(); return t * 12 * Math.PI / 2 * Math.cos(t); }
    public void setDao(IDao dao) { this.dao = dao; }                     // injection par setter
}
```

### 5.a Instanciation statique (`PresStatique`)
Les dépendances sont créées avec `new` dans la couche présentation :
```java
DaoImpl dao = new DaoImpl();
MetierImpl metier = new MetierImpl(dao);
```
```
Version base de données
Résultat = -543.8358768288695
```

### 5.b Instanciation dynamique (`PresDynamique` + `config.txt`)
Les noms des classes sont lus dans `config.txt`. Les objets sont créés par réflexion (`Class.forName` puis `newInstance`), et `setDao` est appelé par réflexion. Pour passer d'une implémentation à l'autre, il suffit de modifier le fichier, sans recompiler.
```
ma.enset.ext.DaoImplV2
ma.enset.metier.MetierImpl
```
```
Version capteurs
Résultat = 190.87526861687965
```

### 5.c Spring, version XML (`PresSpringXML` + `applicationContext.xml`)
```xml
<bean id="dao" class="ma.enset.ext.DaoImplV2"/>
<bean id="metier" class="ma.enset.metier.MetierImpl">
    <property name="dao" ref="dao"/>
</bean>
```
```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
IMetier metier = (IMetier) context.getBean("metier");
```

### 5.c Spring, version annotations (`PresSpringAnnotation`)
Les classes sont annotées `@Component`, et Spring scanne les packages. Il existe deux implémentations de `IDao`, d'où le `@Qualifier("dao")` sur le paramètre du constructeur `@Autowired`.
```java
ApplicationContext context = new AnnotationConfigApplicationContext("ma.enset.dao", "ma.enset.ext", "ma.enset.metier");
IMetier metier = context.getBean(IMetier.class);
```

## Partie 2 : mini framework d'injection des dépendances

### Conception
| Élément | Rôle |
|---|---|
| `ApplicationContext` | Interface du conteneur : `getBean(String)` et `getBean(Class<T>)` |
| `XmlApplicationContext` | Lit le fichier XML avec **JAXB** (`Unmarshaller`), crée les beans en singletons et résout les dépendances récursivement, avec détection des dépendances circulaires |
| `Beans` / `Bean` / `Dependency` | Classes JAXB : `@XmlRootElement`, `@XmlElement`, `@XmlAttribute` (mapping objet/XML) |
| `AnnotationApplicationContext` | Scanne les packages (répertoires ou jar), enregistre les classes `@Component` et injecte les dépendances `@Inject` |
| `@Component("nom")` | Déclare un composant |
| `@Inject` | Point d'injection : **constructeur**, **setter** ou **attribut** |
| `@Qualifier("nom")` | Choisit le bean quand plusieurs implémentations existent |

### Les 3 modes d'injection
| Mode | XML (`mini-beans.xml`) | Annotations |
|---|---|---|
| a- Constructeur | `<constructor-arg ref="daoCapteur"/>` | `@Inject` sur le constructeur, avec `@Qualifier` sur le paramètre |
| b- Setter | `<property name="dao" ref="daoBase"/>`, qui appelle `setDao` | `@Inject @Qualifier("daoCapteur")` sur `setDao` |
| c- Attribut (Field) | `<field name="dao" ref="daoCapteur"/>`, plus `value` pour les types simples | `@Inject @Qualifier("daoBase")` sur l'attribut privé (`setAccessible`) |

### Version XML (`PresFrameworkXML`)
```xml
<beans>
    <bean id="daoBase" class="ma.enset.demo.dao.DaoBase"/>
    <bean id="daoCapteur" class="ma.enset.demo.dao.DaoCapteur"/>
    <bean id="metierConstructeur" class="ma.enset.demo.metier.MetierConstructeur">
        <constructor-arg ref="daoCapteur"/>
    </bean>
    <bean id="metierSetter" class="ma.enset.demo.metier.MetierSetter">
        <property name="dao" ref="daoBase"/>
    </bean>
    <bean id="metierField" class="ma.enset.demo.metier.MetierField">
        <field name="dao" ref="daoCapteur"/>
        <field name="coefficient" value="10"/>
    </bean>
</beans>
```
```
  [DaoCapteur] Version capteurs
metierConstructeur -> résultat = 24.0
  [DaoBase] Version base de données
metierSetter -> résultat = 68.0
  [DaoCapteur] Version capteurs
metierField -> résultat = 240.0
```

### Version annotations (`PresFrameworkAnnotations`)
```java
@Component("metierField")
public class MetierField implements IMetier {
    @Inject @Qualifier("daoBase")
    private IDao dao;
    ...
}
```
```
Composants détectés : [daoCapteur, daoBase, metierSetter, metierField, metierConstructeur]
  [DaoBase] Version base de données
metierConstructeur -> résultat = 68.0
  [DaoCapteur] Version capteurs
metierSetter -> résultat = 24.0
  [DaoBase] Version base de données
metierField -> résultat = 68.0
```
Les mêmes classes métier sont branchées sur des DAO différents selon la configuration, XML ou annotations. Le code métier ne change pas.

## Exécution
```bash
mvn compile
mvn exec:java -Dexec.mainClass=ma.enset.presentation.PresStatique
mvn exec:java -Dexec.mainClass=ma.enset.presentation.PresDynamique
mvn exec:java -Dexec.mainClass=ma.enset.presentation.PresSpringXML
mvn exec:java -Dexec.mainClass=ma.enset.presentation.PresSpringAnnotation
mvn exec:java -Dexec.mainClass=ma.enset.demo.PresFrameworkXML
mvn exec:java -Dexec.mainClass=ma.enset.demo.PresFrameworkAnnotations
```
Ou directement depuis IntelliJ : clic droit sur la classe, puis *Run*.

## Conclusion
- L'instanciation statique crée un couplage dans la couche présentation.
- L'instanciation dynamique supprime ce couplage, mais oblige à écrire soi-même le code de réflexion.
- Spring automatise cette mécanique (IOC).
- Le mini framework reproduit le principe de Spring IOC : lecture d'une configuration (XML avec JAXB ou annotations), création des objets par réflexion, puis injection par constructeur, setter ou attribut.
