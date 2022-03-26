package wang.seamas;

import com.alibaba.excel.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.util.Comparator;

public class ExcelFieldCompare implements Comparator<Field> {

    @Override
    public int compare(Field o1, Field o2) {
        ExcelProperty a1 = o1.getAnnotation(ExcelProperty.class);
        ExcelProperty a2 = o2.getAnnotation(ExcelProperty.class);

        if (a1 == null && a2 == null) {
            return 0;
        }
        if (a1 == null) {
            return -1;
        }
        if (a2 == null) {
            return 1;
        }

        return a1.order() - a2.order();
    }
}
