package ma.enset.framework.context;

/** Conteneur IOC du mini framework. */
public interface ApplicationContext {
    Object getBean(String name);
    <T> T getBean(Class<T> type);
}
