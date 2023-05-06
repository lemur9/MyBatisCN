package interceptor.start;

import interceptor.anno.DictTranslate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldProperty {
    private String name;
    private DictTranslate fieldBind;
}