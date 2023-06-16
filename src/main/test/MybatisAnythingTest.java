import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试一切
 */
public class MybatisAnythingTest {

    @Before
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

    /**
     * 测试逻辑或
     */
    @Test
    public void testOr() {
        int a = 6; // 0 0 0 0 0 1 1 0
        int b = 5; // 0 0 0 0 0 1 0 1
        a |= b;    // 0 0 0 0 0 1 1 1
        System.out.println(a); // 7
    }

    /**
     * 测试Mybatis中的异常处理机制
     */
    @Test
    public void testException() {
        RuntimeException ex = ExceptionFactory.wrapException("测试错误", new RuntimeException("运行时错误"));
        System.out.println(ex);
    }


    /**
     * List的toArray(T[] a)
     */
    @Test
    public void testToArray() {
        List<Class<?>> constructorArgTypes = new ArrayList<>();
        constructorArgTypes.add(MybatisAnythingTest.class);
        constructorArgTypes.add(Long.class);
        constructorArgTypes.add(Readable.class);
        constructorArgTypes.add(Select.class);
        constructorArgTypes.add(Integer.class);
        constructorArgTypes.add(ArrayList.class);

        for (int i = constructorArgTypes.size() - 1; i >= 0; i--) {
            System.out.println(constructorArgTypes.get(i));
        }

        System.out.println();
        System.out.println();

        Class<?>[] classes = constructorArgTypes.toArray(new Class[0]);
        for (int i = classes.length - 1; i >= 0; i--) {
            System.out.println(classes[i]);
        }
    }

    /**
     * Class.isAssignableFrom(Class<?> cls)的使用
     * 如果指定的参数cls恰好是此Class对象，则此方法返回true；否则返回false
     */
    @Test
    public void testIsAssignableFrom() {
        ArrayList<Object> objects = new ArrayList<>();

        System.out.println(objects.getClass().isAssignableFrom(List.class)); //false

        System.out.println();

        System.out.println(List.class.isAssignableFrom(objects.getClass())); //true
    }

    @Test
    public void testInvoker() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        TestModel testModel = new TestModel();
        Field age = testModel.getClass().getDeclaredField("age");
        Invoker testInvoker = new GetFieldInvoker(age);
        Object invoke = testInvoker.invoke(testModel, new Object[0]);
        System.out.println(invoke);
    }

    /**
     * PropertyTokenizer解析出属性
     */
    @Test
    public void testPropertyTokenizer() {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer("student[sId].name");
        System.out.println(propertyTokenizer.getName());
        System.out.println(propertyTokenizer.getIndexedName());
        System.out.println(propertyTokenizer.getIndex());
        System.out.println(propertyTokenizer.getChildren());
    }

    @Test
    public void testMybatis() {
        DataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:mem:mybatis", "sa", "");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(TestModelMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TestModelMapper mapper = sqlSession.getMapper(TestModelMapper.class);
        List<TestModel> testModels = mapper.selectAll("2", "Lemur", "男");
        testModels.forEach(System.out::println);
    }

    /**
     * 不使用配置文件的方式创建sqlSessionFactory
     */
    @Test
    public void testOverWriteNoMapping() throws SQLException {
        Configuration configuration = new Configuration(
                new Environment("default",
                        new JdbcTransactionFactory(),
                        new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "")
                )
        );
        configuration.addMapper(TestModelMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
//        List<TestModel> mapper = sqlSession.selectList("TestModelMapper.selectAll");
//        mapper.forEach(System.out::println);
        Connection connection = sqlSession.getConnection();
        SqlRunner sqlRunner = new SqlRunner(connection);
        List<Map<String, Object>> lists = sqlRunner.selectAll(new SQL().SELECT("*").FROM("user").toString());
        List<Map<String, Object>> list = new ArrayList<>();

        lists.forEach(map -> {
            HashMap<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            list.add(result);
        });

        list.forEach(System.out::println);
    }

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
        TestModelMapper mapper = sqlSession.getMapper(TestModelMapper.class);
        List<TestModel> testModels = mapper.selectAll("2", "Lemur", "男");
        List<TestModel> testModelList = mapper.selectById("2");
        testModels.forEach(System.out::println);
        testModelList.forEach(System.out::println);
    }

    @Test
    public void testStream() {
        List<TestModel> users = new ArrayList<>();
        users.add(new TestModel(1, "小明", 20));
        users.add(new TestModel(2, "小亮", 21));
        users.add(new TestModel(3, "小红", 23));
        users.add(new TestModel(4, "小方", 24));
        users.forEach(System.out::println);
    }

    @Test
    public void testXPath() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(getClass().getClassLoader().getResourceAsStream("TestModel.xml"));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression compile = xPath.compile("/testModels/testModel");

        String evaluate = compile.evaluate(document);
        System.out.println(evaluate);

    }

    @Test
    public void testXPath2() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(getClass().getClassLoader().getResourceAsStream("TestModelMapper.xml"));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression compile = xPath.compile("/configuration/settings[1]");

        String evaluate = compile.evaluate(document);
        System.out.println(evaluate);
    }

    @Test
    public void test() throws SQLException {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        Configuration configuration = new Configuration(new Environment("default", new JdbcTransactionFactory(), new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "")));
        configuration.addMapper(TestModel.class);
        SqlSessionFactory build = sqlSessionFactoryBuilder.build(configuration);
        SqlSession sqlSession = build.openSession();
        Connection connection = sqlSession.getConnection();
        SqlRunner sqlRunner = new SqlRunner(connection);
        List<Map<String, Object>> user = sqlRunner.selectAll(new SQL().SELECT("*").FROM("user").toString());
        user.forEach(System.out::println);
    }

    boolean flag = false;

    @Test
    public void testDebug() {
        for (int i = 1; i < 100; i++) {
            if (flag) {
                System.out.println(i);
            }
        }
    }
}


