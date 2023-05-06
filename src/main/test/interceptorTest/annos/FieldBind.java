package interceptorTest.annos;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface FieldBind {
    String type(); //字典的分类

    String target(); //翻译的目标属性
}