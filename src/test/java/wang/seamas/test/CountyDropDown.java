package wang.seamas.test;

import wang.seamas.DropDownInterface;

import java.util.HashMap;
import java.util.Map;

public class CountyDropDown implements DropDownInterface {

    private static Map<String, String[]> map = new HashMap<>();

    static {
        map.put("杭州", new String[]{ "西湖", "上城" });
        map.put("宁波", new String[]{ "余姚", "慈溪", "奉化" });
        map.put("黄浦", new String[]{ "南京东路", "外滩", "瑞金二路" });
        map.put("徐汇", new String[]{ "湖南路街道", "天平路街道", "枫林路街道" });
    }

    @Override
    public String[] source(String key) {
        return map.getOrDefault(key, new String[0]);
    }
}
