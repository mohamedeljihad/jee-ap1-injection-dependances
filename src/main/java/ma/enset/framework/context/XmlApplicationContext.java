package ma.enset.framework.context;

import ma.enset.framework.xml.Bean;
import ma.enset.framework.xml.Beans;
import ma.enset.framework.xml.Dependency;

import javax.xml.bind.JAXBContext;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Conteneur configuré par un fichier XML lu avec JAXB (OXM).
 * Les beans sont des singletons créés à la demande ; les dépendances sont résolues récursivement.
 */
public class XmlApplicationContext implements ApplicationContext {

    private final Map<String, Bean> definitions = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();
    private final Set<String> inCreation = new HashSet<>();

    public XmlApplicationContext(String classpathResource) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) throw new BeanCreationException("Fichier introuvable : " + classpathResource);
            Beans beans = (Beans) JAXBContext.newInstance(Beans.class).createUnmarshaller().unmarshal(in);
            for (Bean b : beans.getBeans()) definitions.put(b.getId(), b);
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException("Lecture de " + classpathResource + " impossible", e);
        }
        // instanciation de tous les singletons au démarrage (comme Spring)
        for (String id : definitions.keySet()) getBean(id);
    }

    @Override
    public Object getBean(String name) {
        if (singletons.containsKey(name)) return singletons.get(name);
        Bean def = definitions.get(name);
        if (def == null) throw new BeanCreationException("Aucun bean nommé '" + name + "'");
        if (!inCreation.add(name)) throw new BeanCreationException("Dépendance circulaire sur '" + name + "'");
        try {
            Object bean = create(def);
            singletons.put(name, bean);
            return bean;
        } finally {
            inCreation.remove(name);
        }
    }

    @Override
    public <T> T getBean(Class<T> type) {
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, Bean> e : definitions.entrySet()) {
            if (type.isAssignableFrom(loadClass(e.getValue().getClassName()))) matches.add(e.getKey());
        }
        if (matches.size() != 1)
            throw new BeanCreationException(matches.size() + " beans de type " + type.getName() + " : " + matches);
        return type.cast(getBean(matches.get(0)));
    }

    private Object create(Bean def) {
        Class<?> clazz = loadClass(def.getClassName());
        try {
            // 1) constructeur
            Object instance;
            List<Dependency> args = def.getConstructorArgs();
            if (args.isEmpty()) {
                instance = clazz.getDeclaredConstructor().newInstance();
            } else {
                instance = instantiateWithArgs(clazz, args);
            }
            // 2) setters
            for (Dependency p : def.getProperties()) {
                String setter = "set" + Character.toUpperCase(p.getName().charAt(0)) + p.getName().substring(1);
                Method m = findSetter(clazz, setter);
                m.invoke(instance, resolve(p, m.getParameterTypes()[0]));
            }
            // 3) accès direct aux attributs
            for (Dependency f : def.getFields()) {
                Field field = findField(clazz, f.getName());
                field.setAccessible(true);
                field.set(instance, resolve(f, field.getType()));
            }
            return instance;
        } catch (BeanCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException("Création du bean '" + def.getId() + "' impossible", e);
        }
    }

    private Object instantiateWithArgs(Class<?> clazz, List<Dependency> args) throws Exception {
        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.getParameterCount() != args.size()) continue;
            Class<?>[] types = c.getParameterTypes();
            Object[] values = new Object[types.length];
            boolean ok = true;
            for (int i = 0; i < types.length; i++) {
                Object v = resolve(args.get(i), types[i]);
                if (v != null && !wrap(types[i]).isInstance(v)) { ok = false; break; }
                values[i] = v;
            }
            if (ok) return c.newInstance(values);
        }
        throw new BeanCreationException("Aucun constructeur compatible dans " + clazz.getName());
    }

    private Object resolve(Dependency d, Class<?> type) {
        if (d.getRef() != null) return getBean(d.getRef());
        if (d.getValue() != null) return Conversions.convert(d.getValue(), type);
        throw new BeanCreationException("Dépendance sans ref ni value");
    }

    private static Method findSetter(Class<?> clazz, String name) {
        for (Method m : clazz.getMethods())
            if (m.getName().equals(name) && m.getParameterCount() == 1) return m;
        throw new BeanCreationException("Setter " + name + " introuvable dans " + clazz.getName());
    }

    static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) { }
        }
        throw new BeanCreationException("Attribut " + name + " introuvable dans " + clazz.getName());
    }

    static Class<?> wrap(Class<?> t) {
        if (!t.isPrimitive()) return t;
        if (t == int.class) return Integer.class;
        if (t == long.class) return Long.class;
        if (t == double.class) return Double.class;
        if (t == boolean.class) return Boolean.class;
        return t;
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new BeanCreationException("Classe introuvable : " + name, e);
        }
    }
}
