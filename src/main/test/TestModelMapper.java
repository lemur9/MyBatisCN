import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 测试Mapper
 */
public interface TestModelMapper {

    //@Select("select * from user")
    List<TestModel> selectAll(String id, String name, String sex);

    List<TestModel> selectById(String s);
}
