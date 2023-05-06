package interceptorTest;

import interceptorTest.annos.FieldBind;
import org.apache.ibatis.reflection.MetaObject;

public interface DemoDataBind {

    /**
     * 使用反射对象MetaObject，操作目标对象Object
     * @param fieldBind 自定义的注解
     * @param o 原始对象
     * @param metaObject 原始对象的mybatis反射对象
     */
    void setMetaObject(FieldBind fieldBind, Object o, MetaObject metaObject);
}