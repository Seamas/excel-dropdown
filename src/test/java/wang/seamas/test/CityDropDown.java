package wang.seamas.test;

import wang.seamas.DropDownInterface;

import java.util.HashMap;
import java.util.Map;

public class CityDropDown implements DropDownInterface {

    private static Map<String, String[]> map = new HashMap<>();

    static {
        map.put("浙江", new String[] { "杭州", "宁波"});
        map.put("上海", new String[]{ "黄浦", "徐汇"});
    }

    @Override
    public String[] source(String key) {
        return map.getOrDefault(key, new String[0]);
    }
}
