package day01_understand;

import day01_understand.dao.TestModelMapper;
import day01_understand.demo.Dept;
import day01_understand.demo.Employee;
import day01_understand.demo.TestModel;
import day01_understand.utils.StringTypeHandler;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        configuration.getTypeAliasRegistry().registerAliases(TestModel.class.getPackage().getName());
        JdbcTransactionFactory jdbcTransactionFactory = new JdbcTransactionFactory();
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(configuration.getVariables().getProperty("driver"));
        pooledDataSource.setUrl(configuration.getVariables().getProperty("url"));
        pooledDataSource.setUsername(configuration.getVariables().getProperty("username"));
        pooledDataSource.setPassword(configuration.getVariables().getProperty("password"));
        Environment environment = new Environment("development", jdbcTransactionFactory, pooledDataSource);
        configuration.setEnvironment(environment);


        //1.直接构建MappedStatement对象
        /*SqlSource staticSqlSource = new StaticSqlSource(configuration, "select * from user");
        ResultMap resultMap = new ResultMap.Builder(configuration, "defaultResultMap", TestModel.class, new ArrayList<>()).build();
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, "day01_understand.dao.TestModelMapper.selectAll", staticSqlSource, SqlCommandType.SELECT)
                .resultMaps(Collections.singletonList(resultMap))
                .build();
        configuration.addMappedStatement(mappedStatement);*/

        //2.交由mybatis解析并构建出MappedStatement对象
        configuration.addMappers(TestModelMapper.class.getPackage().getName());
        return configuration;
    }

    @BeforeEach
    public void initData() {
        try {
            // 加载HSQLDB驱动
            Class.forName("org.hsqldb.jdbcDriver");
            // 获取Connection对象
            Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:mybatis",
                    "sa", "");
            // 使用Mybatis的ScriptRunner工具类执行数据库脚本
            ScriptRunner scriptRunner = new ScriptRunner(conn);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(Resources.getResourceAsReader("create-table.sql"));
            scriptRunner.runScript(Resources.getResourceAsReader("init-data.sql"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        //1.直接通过MappedStatement调用
        List<Object> objects = sqlSession.selectList("day01_understand.dao.TestModelMapper.selectAll");
        System.out.println(objects);

        //2.通过MapperRegistry的knownMappers找到对应的MapperProxyFactory构建Dao接口的代理对象,
        // 通过MapperMethod解析Dao接口方法并存储信息,然后来执行具体的SQL操作,最后返回方法返回值类型所对应的结果。
        TestModelMapper mapper = sqlSession.getMapper(TestModelMapper.class);
        List<TestModel> testModels = mapper.selectAll();
        System.out.println(testModels);
    }

    @Test
    public void tesXMLConfigBuilder() throws IOException {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(Resources.getResourceAsReader("mybatis.xml"));
        Configuration configuration = xmlConfigBuilder.parse();


        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);
        sqlSession = sqlSessionFactory.openSession(true);

        testConfiguration();
    }

    @Test
    public void testOgnl() throws OgnlException {
        //Ognl保证3.3.3以上,否则必须实现MemberAccess接口
        TestModel testModel = new TestModel(1, "2023", "lemur", "123", "123456", "limu");
        Object name = Ognl.getValue("name", testModel);
        System.out.println("name = " + name);
    }

    @Test
    public void testMetaObject() {
        Object employee = new Employee(1, "lemur", new Dept(1, "人力资源部门"));
        MetaObject metaObject = MetaObject.forObject(employee,
                new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory()
        );

        //1、修改dept的属性
        metaObject.setValue("dept.name", "研发部");
        System.out.println(employee);
    }

    @Test
    public void testErrorContext() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int j = i;
            executorService.execute(() -> {
                try {
                    ErrorContext.instance()
                            .activity("在第[" + j + "]个线程中")
                            .object(this.getClass().getName())
                            .sql("slelct * from user")
                            .resource("TestModelMapper.xml");

                    if (new Random().nextInt(10) > 6) {
                        int x = 1 / 0;
                    }
                } catch (Exception e) {
                    throw ExceptionFactory.wrapException("测试错误", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
    }

    @Test
    public void testMappedStatement() throws Exception {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(Resources.getResourceAsReader("mybatis.xml"));
        Configuration configuration = xmlConfigBuilder.parse();

        String sql = "select * from user";
        SqlSource staticSqlSource = new StaticSqlSource(configuration, sql);

        ResultMapping.Builder idBuilder = new ResultMapping.Builder(configuration, "id","id",new StringTypeHandler());
        ResultMapping resultMapping = idBuilder.build();

        List<ResultMapping> resultMappings = new ArrayList<>();
        resultMappings.add(resultMapping);

        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, "selectUser", TestModel.class, resultMappings);

        Discriminator discriminator = new Discriminator.Builder(configuration, resultMapping, new HashMap<>()).build();

        resultMapBuilder.discriminator(discriminator);
        ResultMap resultMap = resultMapBuilder.build();

        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(resultMap);

        MappedStatement.Builder mappedStatementBuilder = new MappedStatement.Builder(configuration, "select", staticSqlSource, SqlCommandType.SELECT);
        mappedStatementBuilder.resultMaps(resultMaps);
        MappedStatement mappedStatement = mappedStatementBuilder.build();

    }
}
