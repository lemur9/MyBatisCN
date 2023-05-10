package day01_understand.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestModel {
    private Integer id;
    private String create_time;
    private String name;
    private String password;
    private String phone;
    private String nick_nam;
}
