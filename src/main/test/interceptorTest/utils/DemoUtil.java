package interceptorTest.utils;

import interceptor.anno.DictTranslate;
import interceptor.start.FieldProperty;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DemoUtil {
    private static Map<Class<?>, List<FieldProperty>> fieldProMaps;
    private static Set<Class<?>> invalidClass;

    static {
        //缓存class和属性，不用每次都遍历查找
        fieldProMaps = new ConcurrentHashMap<>();
        //不合法的class，有些class天生就不可能有我们自定义的注解，例如HashMap
        //如果第一个遍历class属性之后发现没有自定义注解，也会被标记为不合法的class
        invalidClass = new CopyOnWriteArraySet<>();
        //不校验HashMap
        invalidClass.add(HashMap.class);
    }


    /**
     * 是否需要翻译，通过寻找class上的自定义注解
     */
    public static boolean needTranslate(Configuration configuration, Object obj, BiConsumer<MetaObject, FieldProperty> biConsumer) {
        //根据对象的
        List<FieldProperty> fieldPropertyList = getFieldPropertyList(obj.getClass());
        if (!Objects.isNull(fieldPropertyList)) {
            //如果不为空的话，调用BiConsumer的apply了
            //创建元数据对象（为什么要花很大功夫得到mybatis的Configuration？自己写反射不也可以完成吗？因为mybatis可能还有很多其他配置，自己写可能会丢失那些功能，这些配置都在Configuration里了，newMetaObject也会有缓存在其中）
            MetaObject metaObject = configuration.newMetaObject(obj);
            //多线程处理，fork join
            fieldPropertyList.parallelStream().forEach(fieldProperty -> biConsumer.accept(metaObject, fieldProperty));
            return true;
        }
        return false;
    }


    /**
     * 找翻译注解
     */
    private static List<FieldProperty> getFieldPropertyList(Class<?> clazz) {
        if (invalidClass.contains(clazz)) {
            //检查是否合法
            return null;
        }

        //缓存检查
        List<FieldProperty> fieldProperties = fieldProMaps.get(clazz);
        if (fieldProperties != null) {
            return fieldProperties;
        }

        //获取到所有的属性
        List<Field> allField = getAllField(clazz);
        //过滤出有FieldBind的属性，并且封装成FieldProperty
        List<FieldProperty> collect = allField.stream().filter(field -> {
                    DictTranslate annotation = field.getAnnotation(DictTranslate.class);
                    return annotation != null;
                }).map(field -> new FieldProperty(field.getName(), field.getAnnotation(DictTranslate.class)))
                .collect(Collectors.toList());
        //空的话，不合法
        if (collect.size() == 0) {
            invalidClass.add(clazz);
        } else {
            //不为空，存缓存
            fieldProMaps.put(clazz, collect);
        }
        return collect;
    }

    /**
     * 找到所有field,包括父类
     *
     * @return
     */
    private static List<Field> getAllField(Class<?> clazz) {

        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fieldList.stream().filter((field) -> {
            //去除static属性
            return !Modifier.isStatic(field.getModifiers());
        }).filter((field) -> {
            //去除transient属性
            return !Modifier.isTransient(field.getModifiers());
        }).collect(Collectors.toList());
    }
}