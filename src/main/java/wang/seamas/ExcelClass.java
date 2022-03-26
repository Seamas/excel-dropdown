package wang.seamas;

import lombok.Getter;
import lombok.Setter;
import wang.seamas.struct.DirectGraph;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public class ExcelClass {

    private Class sourceClazz;
    private Map<String, Field> nameMap;
    private Map<Field, Integer> fieldMap;
    private Queue<Field> dropDownList;
    private boolean depends;

    private ExcelClass(Class sourceClazz) throws Exception {
        this.sourceClazz = sourceClazz;
        nameMap = new HashMap<>();
        fieldMap = new HashMap<>();

        Field[] fields = ReflectionUtil.getDeclaredFields(this.sourceClazz);

        List<Field> fieldList = Arrays.stream(fields)
                .sorted(new ExcelFieldCompare())
                .collect(Collectors.toList());

        for(int i = 0; i < fieldList.size(); i++) {
            Field field = fieldList.get(i);
            fieldMap.put(field, i);
            nameMap.put(field.getName(), field);
        }

        Set<Field> set = fieldMap.keySet().stream()
                .filter(item -> item.getAnnotation(DropDownSetField.class) != null)
                .collect(Collectors.toSet());

        DirectGraph<Field> directGraph = new DirectGraph<>();

        set.forEach(item -> {
            directGraph.addVertex(item);

            DropDownSetField annotation = item.getAnnotation(DropDownSetField.class);
            String deps = annotation.deps();
            if (deps != null && deps.length() > 0) {
                Field field = nameMap.get(deps);
                directGraph.addEdge(item, field);
                depends = true;
            }
        });
        dropDownList = directGraph.topologicalSort();
    }

    private static ConcurrentHashMap<Class, ExcelClass> cache = new ConcurrentHashMap<>();

    public static ExcelClass getExcelClass(Class sourceClazz) throws Exception {
        ExcelClass instance = cache.get(sourceClazz);
        if (instance == null) {
            instance = new ExcelClass(sourceClazz);
            cache.put(sourceClazz, instance);
        }
        return instance;
    }
}
