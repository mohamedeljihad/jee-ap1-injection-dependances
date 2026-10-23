package ma.enset.framework.context;

/** Conversion des valeurs littérales (attribut value) vers le type attendu. */
final class Conversions {
    private Conversions() { }

    static Object convert(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        throw new BeanCreationException("Conversion impossible de '" + value + "' vers " + type.getName());
    }
}
