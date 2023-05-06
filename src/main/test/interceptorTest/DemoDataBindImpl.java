package interceptorTest;

import interceptorTest.annos.FieldBind;
import interceptorTest.utils.DemoUtil;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DemoDataBindImpl implements DemoDataBind{
    public interface BindType {
        String USER_STATUS = "user_status";

    }
    /**
     * 可以在系统启动的时候，将数据库的所有字典和java代码中的枚举缓存起来
     * 分布式记得用redis，更新的时候记得要更新redis(一般很少更新字典)
     */
    private Map<String, String> STATUS_MAP = new ConcurrentHashMap<String, String>() {{
        put("18", "壮年");
        put("1", "正常");
    }};


    @Override
    public void setMetaObject(FieldBind fieldBind, Object fieldValue, MetaObject metaObject) {
        // 数据库中数据转换
        if (BindType.USER_STATUS.equals(fieldBind.type())) {
            //使用反射工具类修改target属性，值从缓存中取到对应的结果
            metaObject.setValue(fieldBind.target(), STATUS_MAP.get(String.valueOf(fieldValue)));
        }
    }
}
