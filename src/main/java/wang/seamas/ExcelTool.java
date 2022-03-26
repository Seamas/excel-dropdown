package wang.seamas;

import com.alibaba.excel.annotation.ExcelProperty;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelTool {

    private static final int XLS_MAX_ROW = 60000;

    public static void writeExcelTemplate(Class clazz, ServiceResolver resolver, OutputStream outputStream) throws Exception {
        ExcelClass excelClass = ExcelClass.getExcelClass(clazz);

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet");
        buildTitle(excelClass, sheet);
        buildConstraint(resolver, excelClass, sheet);

        workbook.write(outputStream);
    }

    private static void buildConstraint(ServiceResolver resolver, ExcelClass excelClass, HSSFSheet sheet) throws InstantiationException, IllegalAccessException {
        Queue<Field> queue = excelClass.getDropDownList();
        Set<Field> proceed = new HashSet<>();
        Map<Field, String[]> dropDownMap = new HashMap<>();
        excelClass.isDepends();
        while (!queue.isEmpty()) {
            Field field = queue.poll();
            processFieldConstraint(resolver, field, excelClass, sheet, proceed, dropDownMap);
        }
    }

    private static void processFieldConstraint(ServiceResolver resolver, Field field, ExcelClass excelClass, HSSFSheet sheet, Set<Field> proceed, Map<Field, String[]> dropDownMap)
            throws InstantiationException, IllegalAccessException {
        if (proceed.contains(field))
            return;

        DropDownSetField annotation = field.getAnnotation(DropDownSetField.class);
        String[] source = annotation.source();
        if (source != null && source.length > 0) {
            createStaticConstraint(sheet, source, excelClass, field, annotation.multiply());
            dropDownMap.put(field, source);
        } else {
            String deps = annotation.deps();
            Class<? extends DropDownInterface> sourceClass = annotation.sourceClass()[0];
            DropDownInterface dropDownService = resolver.resolve(sourceClass);
            if (deps != null && deps.length() > 0) {
                Field depsField = excelClass.getNameMap().get(deps);
                processFieldConstraint(resolver, depsField, excelClass, sheet, proceed, dropDownMap);
                String[] values = dropDownMap.get(depsField);
                // 创建级联的map
                Map<String, String[]> map = new HashMap<>();
                for(String value: values) {
                    String[] strings = dropDownService.source(value);
                    map.put(value, strings);
                }
                // 创建隐藏目录
                createHideSheetHSSF(sheet, map, field.getName());

                int columnIndex = excelClass.getFieldMap().get(field);
                CellRangeAddressList rangeAddressList = new CellRangeAddressList(1, XLS_MAX_ROW, columnIndex, columnIndex);
                int depColumnIndex = excelClass.getFieldMap().get(depsField);
                String columnName = getColumnName(depColumnIndex);
                DataValidation dataValidation = new HSSFDataValidation(rangeAddressList, DVConstraint.createFormulaListConstraint("INDIRECT($" + columnName+ "1)"));
                dataValidation.createErrorBox("错误", "请选择正确的" + getFieldName(field));
                dataValidation.setShowErrorBox(true);
                sheet.addValidationData(dataValidation);

                List<String> strings = map.values().stream().flatMap(Arrays::stream)
                        .collect(Collectors.toList());
                dropDownMap.put(field, strings.toArray(new String[0]));
            } else {
                String key = annotation.key();
                String[] strings = dropDownService.source(key);

                createStaticConstraint(sheet, strings, excelClass, field, annotation.multiply());
                dropDownMap.put(field, strings);
            }

        }

        proceed.add(field);
    }

    private static void createHideSheetHSSF(Sheet sheet, Map<String, String[]> constraintMap, String sheetName) {
        Workbook workbook = sheet.getWorkbook();
        Sheet hideSheet = workbook.createSheet(sheetName);

        int rowIndex = 0;

        for (Map.Entry<String, String[]> entry : constraintMap.entrySet()) {
            Row row = hideSheet.createRow(rowIndex++);
            int columnIndex = 0;
            String[] value = entry.getValue();
            for (String s : value) {
                row.createCell(columnIndex++).setCellValue(s);
            }
            String range = getRange(0, rowIndex, value.length);
            Name name = sheet.getWorkbook().createName();
            name.setNameName(entry.getKey());
            String formula = sheetName + "!" + range;
            name.setRefersToFormula(formula);
        }
        int sheetIndex = workbook.getSheetIndex(hideSheet);
        workbook.setSheetHidden(sheetIndex, true);
    }

    /**
     * 创建静态的约束
     * @param strings
     * @param excelClass
     * @param field
     */
    private static void createStaticConstraint(Sheet sheet, String[] strings, ExcelClass excelClass, Field field, boolean isMultiply) {
        // TODO: 经查阅资料, poi生成excel, 不支持多选的操作
        DVConstraint constraint = DVConstraint.createExplicitListConstraint(strings);
        Integer columnIndex = excelClass.getFieldMap().get(field);
        CellRangeAddressList addressList = new CellRangeAddressList(1, XLS_MAX_ROW, columnIndex, columnIndex);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, constraint);
        dataValidation.createErrorBox("错误", "请选择" + getFieldName(field));
        dataValidation.setShowErrorBox(true);
        sheet.addValidationData(dataValidation);
    }

    private static void buildTitle(ExcelClass excelClass, HSSFSheet sheet) {
        HSSFCellStyle style = getStyle(sheet.getWorkbook());
        sheet.setDefaultColumnWidth(18);
        HSSFRow row = sheet.createRow(0);

        for (Map.Entry<Field, Integer> entry : excelClass.getFieldMap().entrySet()) {
            HSSFCell cell = row.createCell(entry.getValue());
            cell.setCellValue(getFieldName(entry.getKey()));
            cell.setCellStyle(style);
        }
    }

    private static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        cellStyle.setFont(font);

        return cellStyle;
    }

    private static String getFieldName(Field field) {
        ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
        if (annotation != null) {
            return annotation.value()[0];
        }
        return field.getName();
    }

    private static String getRange(int offset, int rowId, int colCount) {
        String start = getColumnName(offset);
        String end = getColumnName(offset + colCount - 1);
        return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
    }

    private static String getColumnName(int offset) {
        StringBuilder builder = new StringBuilder();
        do {
            builder.insert(0, (char) ('A' + offset % 26 ));
            offset = (offset /26) -1;
        }while (offset >= 0);

        return builder.toString();
    }





}
