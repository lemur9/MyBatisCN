package interceptor;

import interceptor.mapper.UserMapper;
import interceptor.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestMain {
    /**
     * 传统方式使用Mybatis
     *
     * @throws IOException
     */
    @Test
    public void testMybatisMapping() throws IOException {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sessionFactory = builder.build(Resources.getResourceAsReader("mybatis.xml"));
        SqlSession sqlSession = sessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        List<User> testModels = mapper.selectAll();
        testModels.forEach(System.out::println);
    }
}
