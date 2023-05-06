package interceptorTest;

import interceptorTest.annos.FieldBind;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldProperty {
    private String name;
    private FieldBind fieldBind;
}