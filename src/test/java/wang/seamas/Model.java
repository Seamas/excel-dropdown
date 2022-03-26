package wang.seamas;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import wang.seamas.test.CityDropDown;
import wang.seamas.test.CountyDropDown;


@Getter
@Setter
public class Model {

    @ExcelProperty(value = "姓名", order = 1)
    private String name;

    @DropDownSetField(source = { "浙江", "上海"})
    @ExcelProperty(value = "省", order = 2)
    private String province;

    @DropDownSetField(sourceClass = CityDropDown.class, deps = "province")
    @ExcelProperty(value = "市", order = 3)
    private String city;

    @DropDownSetField(sourceClass = CountyDropDown.class, deps = "city")
    @ExcelProperty(value = "县", order = 4)
    private String county;
}
