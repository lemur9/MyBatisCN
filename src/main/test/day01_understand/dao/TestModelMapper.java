package day01_understand.dao;

import day01_understand.demo.TestModel;

import java.util.List;

/**
 * 测试Mapper
 */
public interface TestModelMapper {

    //@Select("select * from user")
    List<TestModel> selectAll();
}
