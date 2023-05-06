package interceptor.anno;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface DictTranslate {

     int[] dictKey();

     String[] dictValue();

     //设置转移字段后缀，默认字典字段追加Txt
     String suffix() default "Txt";
}
