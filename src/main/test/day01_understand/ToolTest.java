package day01_understand;

import day01_understand.dao.TestModelMapper;
import day01_understand.demo.TestModel;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class ToolTest {

    SqlSession sqlSession;

    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        Properties properties = new Properties();
        properties.put("driver", "org.hsqldb.jdbcDriver");
        properties.put("url", "jdbc:hsqldb:mem:mybatis");
        properties.put("username", "sa");
        properties.put("password", "");
        configuration.setVariables(properties);
        configuration.setLogImpl(Slf4jImpl.class);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.getTypeAliasRegistry().registerAliases("day01_understand.demo");
        configuration.getTypeAliasRegistry().registerAliases("day01_understand.dao");
        JdbcTransactionFactory jdbcTransactionFactory = new JdbcTransactionFactory();
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(configuration.getVariables().getProperty("driver"));
        pooledDataSource.setUrl(configuration.getVariables().getProperty("url"));
        pooledDataSource.setUsername(configuration.getVariables().getProperty("username"));
        pooledDataSource.setPassword(configuration.getVariables().getProperty("password"));
        Environment environment = new Environment("development", jdbcTransactionFactory, pooledDataSource);
        configuration.setEnvironment(environment);


        SqlSource staticSqlSource = new StaticSqlSource(configuration, "select * from user");
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, "TestModelMapper.selectAll", staticSqlSource, SqlCommandType.SELECT).build();
        configuration.addMappedStatement(mappedStatement);
        return configuration;
    }

    @BeforeEach
    public void before() {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(getConfiguration());
        sqlSession = sqlSessionFactory.openSession(true);
    }

    @Test
    public void testXml() {
        try {
            XPathParser xPathParser = new XPathParser(Resources.getResourceAsStream("mybatis.xml"), true, null, new XMLMapperEntityResolver());
            XNode xNode = xPathParser.evalNode("/configuration/typeAliases");
            System.out.println(xNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConfiguration() {
        TestModelMapper mapper = sqlSession.getMapper(TestModelMapper.class);
        List<TestModel> testModels = mapper.selectAll();
        System.out.println(testModels);
    }

}
