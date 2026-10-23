package ma.enset.framework.context;

import ma.enset.framework.annotations.Component;
import ma.enset.framework.annotations.Inject;
import ma.enset.framework.annotations.Qualifier;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Conteneur configuré par annotations : scanne les packages donnés, enregistre les classes @Component,
 * puis injecte les dépendances marquées @Inject (constructeur, setter ou attribut).
 */
public class AnnotationApplicationContext implements ApplicationContext {

    private final Map<String, Class<?>> components = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();
    private final Set<String> inCreation = new HashSet<>();

    public AnnotationApplicationContext(String... basePackages) {
        for (String pkg : basePackages) {
            for (Class<?> c : scan(pkg)) {
                Component comp = c.getAnnotation(Component.class);
                if (comp == null || c.isInterface() || Modifier.isAbstract(c.getModifiers())) continue;
                String name = comp.value().isEmpty() ? decapitalize(c.getSimpleName()) : comp.value();
                if (components.containsKey(name)) throw new BeanCreationException("Nom de bean en double : " + name);
                components.put(name, c);
            }
        }
        for (String name : components.keySet()) getBean(name);
    }

    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(components.keySet());
    }

    @Override
    public Object getBean(String name) {
        if (singletons.containsKey(name)) return singletons.get(name);
        Class<?> clazz = components.get(name);
        if (clazz == null) throw new BeanCreationException("Aucun composant nommé '" + name + "'");
        if (!inCreation.add(name)) throw new BeanCreationException("Dépendance circulaire sur '" + name + "'");
        try {
            Object bean = create(clazz);
            singletons.put(name, bean);
            return bean;
        } finally {
            inCreation.remove(name);
        }
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return type.cast(resolveByType(type, null, type.getName()));
    }

    private Object create(Class<?> clazz) {
        try {
            Object instance = instantiate(clazz);
            // injection directe dans les attributs @Inject
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (!f.isAnnotationPresent(Inject.class)) continue;
                    f.setAccessible(true);
                    f.set(instance, resolveByType(f.getType(), f.getAnnotation(Qualifier.class), f.toString()));
                }
            }
            // injection par les setters @Inject
            for (Method m : clazz.getMethods()) {
                if (!m.isAnnotationPresent(Inject.class)) continue;
                if (m.getParameterCount() != 1)
                    throw new BeanCreationException("Le setter @Inject " + m + " doit avoir un seul paramètre");
                Qualifier q = m.getAnnotation(Qualifier.class);
                if (q == null) q = qualifierOf(m.getParameterAnnotations()[0]);
                m.invoke(instance, resolveByType(m.getParameterTypes()[0], q, m.toString()));
            }
            return instance;
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException("Création de " + clazz.getName() + " impossible", e);
        }
    }

    /** Constructeur @Inject s'il existe, sinon le constructeur sans argument. */
    private Object instantiate(Class<?> clazz) throws Exception {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (!c.isAnnotationPresent(Inject.class)) continue;
            Class<?>[] types = c.getParameterTypes();
            Annotation[][] annotations = c.getParameterAnnotations();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++)
                args[i] = resolveByType(types[i], qualifierOf(annotations[i]), c + " param " + i);
            c.setAccessible(true);
            return c.newInstance(args);
        }
        Constructor<?> c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private Object resolveByType(Class<?> type, Qualifier qualifier, String where) {
        if (qualifier != null) {
            Object bean = getBean(qualifier.value());
            if (!type.isInstance(bean))
                throw new BeanCreationException("Le bean '" + qualifier.value() + "' n'est pas de type " + type.getName());
            return bean;
        }
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, Class<?>> e : components.entrySet())
            if (type.isAssignableFrom(e.getValue())) matches.add(e.getKey());
        if (matches.isEmpty()) throw new BeanCreationException("Aucun composant de type " + type.getName() + " pour " + where);
        if (matches.size() > 1)
            throw new BeanCreationException("Plusieurs composants " + matches + " pour " + where + " : utiliser @Qualifier");
        return getBean(matches.get(0));
    }

    private static Qualifier qualifierOf(Annotation[] annotations) {
        for (Annotation a : annotations) if (a instanceof Qualifier) return (Qualifier) a;
        return null;
    }

    private static String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    // ------------------------------------------------------------------ scan du classpath
    private static List<Class<?>> scan(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                        String n = e.nextElement().getName();
                        if (n.startsWith(path + "/") && n.endsWith(".class") && !n.contains("$"))
                            classes.add(Class.forName(n.replace('/', '.').substring(0, n.length() - 6)));
                    }
                } else {
                    walk(new File(URLDecoder.decode(url.getFile(), "UTF-8")), basePackage, classes);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new BeanCreationException("Scan du package " + basePackage + " impossible", e);
        }
        return classes;
    }

    private static void walk(File dir, String pkg, List<Class<?>> out) throws ClassNotFoundException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) walk(f, pkg + "." + f.getName(), out);
            else if (f.getName().endsWith(".class") && !f.getName().contains("$"))
                out.add(Class.forName(pkg + "." + f.getName().substring(0, f.getName().length() - 6)));
        }
    }
}
