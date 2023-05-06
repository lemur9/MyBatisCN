package interceptor.start;

import interceptor.anno.DictTranslate;
import interceptorTest.DemoDataBind;
import interceptorTest.DemoDataBindImpl;
import interceptorTest.utils.DemoUtil;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;

@Intercepts({@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class}
)})
public class DictInterceptor implements Interceptor {

    //字典数据绑定
    private DemoDataBind demoDataBind;

    public DictInterceptor() {
        demoDataBind = new DemoDataBindImpl();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //一定是list结果
        List<?> proceed = (List<?>) invocation.proceed();
        if (proceed.isEmpty())
            return proceed;
        else {
            //获取mybatis的Configuration，用于获得MetaData
            DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) invocation.getTarget();
            Field mappedStatement = resultSetHandler.getClass().getDeclaredField("mappedStatement");
            mappedStatement.setAccessible(true);
            MappedStatement o = (MappedStatement) mappedStatement.get(resultSetHandler);
            Configuration configuration = o.getConfiguration();
            // 迭代处理
            for (Object obj : proceed) {
                //检查是否需要翻译，是否需要翻译的标准是，检查目标对象的Class是否有自定义的注解，
                //有的话，调用字典数据绑定，取修改对象的target属性
                if (null != obj && !DemoUtil.needTranslate(configuration, obj,
                        (metaObject, fieldProperty) -> {
                            //得到自定义注解
                            DictTranslate dictTranslate = fieldProperty.getFieldBind();
                            //得到具体属性的值
                            Object value = metaObject.getValue(fieldProperty.getName());
                            setMetaObject(dictTranslate, value, metaObject);
                        }
                )) {
                    //不需要翻译的话，跳过
                    break;
                }
            }
            return proceed;
        }
    }

    public void setMetaObject(DictTranslate dictTranslate, Object fieldValue, MetaObject metaObject) {

        int[] dictKey = dictTranslate.dictKey();
        String[] dictValue = dictTranslate.dictValue();
        for (int i = 0; i < dictKey.length; i++) {
            int key = dictKey[i];
            String value = dictValue[i];
            //metaObject.setValue(dictTranslate.target(), STATUS_MAP.get(String.valueOf(fieldValue)));
        }
    }
}