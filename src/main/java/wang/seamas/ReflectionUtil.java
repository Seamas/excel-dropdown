package wang.seamas;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {

    private static final ConcurrentHashMap<Class, Field[]> cache = new ConcurrentHashMap<>();

    public static Field[] getDeclaredFields(Class clazz) {
        Field[] result = cache.get(clazz);
        if (result == null) {
            if (clazz == Object.class) {
                cache.put(clazz, new Field[0]);
            } else {
                Field[] fields = clazz.getDeclaredFields();
                Field[] superFields = getDeclaredFields(clazz.getSuperclass());
                List<Field> array = new ArrayList<>();
                array.addAll(Arrays.asList(fields));
                array.addAll(Arrays.asList(superFields));
                cache.put(clazz, array.toArray(new Field[0]));
            }
        }
        return cache.get(clazz);
    }
}
