package day01_understand.handler;

import day01_understand.demo.Dept;
import day01_understand.demo.Employee;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Employee.class)
public class EmployeeHandler extends BaseTypeHandler<Employee> {


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Employee parameter, JdbcType jdbcType) throws SQLException {
        //ps.setInt(i, parameter.getId());
    }

    @Override
    public Employee getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");

        Dept dept = new Dept();
        dept.setId(rs.getInt("dept_id"));
        dept.setName(rs.getString("dept_name"));

        return new Employee(id, name, dept);
    }

    @Override
    public Employee getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Employee getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
