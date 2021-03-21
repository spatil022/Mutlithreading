package com.blz.AddMultiEmplUsingThread;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBService {
	private PreparedStatement employeePayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService;

	private EmployeePayrollDBService() {

	}

	public static Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/service_payroll?useSSl=false";
		String userName = "root";
		String password = "2000";
		Connection connection;
		System.out.println("connecting tothe database:" + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("connection is successful!!!!" + connection);
		return connection;
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate startDate, LocalDate endDate)
			throws PayrollServiceException {
		String sql = String.format("select * from employee_payroll where start between '%s' and '%s';",
				Date.valueOf(startDate), Date.valueOf(endDate));
//		String sql = String.format(
//				"SELECT e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join "
//						+ "employee_department ed on e.id=ed.employee_id inner join department d on ed.dept_id=d.dept_id where start between '%s' AND '%s';",
//				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getEmployeePayrollDataUsingDB(sql);
	}

	public Map<String, Double> getAverageSalaryByGender() throws PayrollServiceException {
		String sql = "select gender,avg(salary) as avg_salary from employee_payroll group by gender";
		return getAggregateByGender("gender", "avg_salary", sql);
	}

	public Map<String, Double> getAggregateByGender(String gender, String aggregate, String sql) {
		Map<String, Double> genderCountMap = new HashMap<>();
		try (Connection connection = employeePayrollDBService.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while (result.next()) {
				String getgender = result.getString(gender);
				Double count = result.getDouble(aggregate);
				genderCountMap.put(getgender, count);
			}
		} catch (SQLException e) {
			e.getMessage();
		}
		return genderCountMap;
	}

	public Map<String, Double> getCountByGender() {
		String sql = "select gender,count(salary) as count_gender from employee_payroll group by gender";
		return getAggregateByGender("gender", "count_gender", sql);
	}

	public Map<String, Double> getMinimumByGender() {
		String sql = "select gender,min(salary) as minSalary_gender from employee_payroll group by gender";
		return getAggregateByGender("gender", "minSalary_gender", sql);
	}

	public Map<String, Double> getMaximumByGender() {
		String sql = "select gender,max(salary) as maxSalary_gender from employee_payroll group by gender";
		return getAggregateByGender("gender", "maxSalary_gender", sql);
	}

	public Map<String, Double> getSalarySumByGender() {
		String sql = "select gender,sum(salary) as sumSalary_gender from employee_payroll group by gender";
		return getAggregateByGender("gender", "sumSalary_gender", sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) throws PayrollServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = employeePayrollDBService.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				Double salary = result.getDouble("salary");
				LocalDate startDate = result.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
			}
		} catch (SQLException e) {
			throw new PayrollServiceException(e.getMessage(), PayrollServiceException.ExceptionType.RETRIEVAL_PROBLEM);
		}
		return employeePayrollList;

	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet result) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		List<String> departmentName = new ArrayList<>();
		try {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				Double salary = result.getDouble("salary");
				LocalDate startDate = result.getDate("start").toLocalDate();
				String gender = result.getString("gender");
				//String dept = result.getString("dept_name");
				//departmentName.add(dept);
				//String[] deptArray = new String[departmentName.size()];
				/*employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate, gender,
						departmentName.toArray(deptArray)));*/
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate, gender));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
//			String sql = "SELECT e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join "
//					+ "employee_department ed on e.id=ed.employee_id inner join department d on ed.dept_id=d.dept_id WHERE name=?";
			String sql = "select * from employee_payroll where name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public List<EmployeePayrollData> readData() throws PayrollServiceException {
		String sql = "SELECT * from employee_payroll ";
		return this.getEmployeePayrollDataUsingQuery(sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingQuery(String sql) {
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = EmployeePayrollDBService.getConnection();) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet result = preparedStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public int updateEmployeeData(String name, double salary) throws PayrollServiceException {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	private int updateEmployeeDataUsingStatement(String name, double salary) throws PayrollServiceException {
		String sql = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);
		try {
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new PayrollServiceException(e.getMessage(), PayrollServiceException.ExceptionType.UPDATE_PROBLEM);
		}
	}

	public int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		try (Connection connection = this.getConnection();) {
			String sql = "update employee_payroll set salary=? where name=?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setDouble(1, salary);
			preparedStatement.setString(2, name);
			int status = preparedStatement.executeUpdate();
			return status;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender)
			throws PayrollServiceException {
		int employeeId = -1;
		EmployeePayrollData employeePayrollData = null;
		String sql = String.format(
				"insert into employee_payroll (name,gender,salary,start)" + "values ('%s', '%s', '%s', '%s')", name,
				gender, salary, Date.valueOf(startDate));
		try (Connection connection = employeePayrollDataStatement.getConnection()) {
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(employeeId, name, salary, startDate);
		} catch (SQLException e) {
			throw new PayrollServiceException(e.getMessage(), PayrollServiceException.ExceptionType.INSERTION_PROBLEM);
		}
		return employeePayrollData;
	}
	
	/*public List<EmployeePayrollData> readActiveEmployeeData() {
		String sql = "select e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join"
				+ " employee_department ed on e.id=ed.employee_id inner join department d on ed.dept_id=d.dept_id where is_active=true ;";
		return this.getEmployeePayrollDataUsingQuery(sql);
	}*/
}