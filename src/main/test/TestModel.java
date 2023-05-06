import interceptorTest.DemoDataBindImpl;
import interceptorTest.annos.FieldBind;

/**
 * 测试实体类
 */
public class TestModel {
    private Integer id;

    public String name;

    public int age = 18;

    public TestModel() {
    }

    public TestModel(Integer id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
