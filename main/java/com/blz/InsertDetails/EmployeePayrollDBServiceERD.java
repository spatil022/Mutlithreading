package com.blz.InsertDetails;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class EmployeePayrollDBServiceERD {
	private static EmployeePayrollDBServiceERD employeePayrollDBServiceERD;

	private EmployeePayrollDBServiceERD() {
	}

	public static EmployeePayrollDBServiceERD getInstance() {
		if (employeePayrollDBServiceERD == null) {
			employeePayrollDBServiceERD = new EmployeePayrollDBServiceERD();
		}
		return employeePayrollDBServiceERD;
	}

	@SuppressWarnings("finally")
	public EmployeePayrollData addEmployeeToPayroll(String name,double salary, LocalDate startDate,  String gender )
			throws PayrollServiceException {
		Connection[] connection = new Connection[1];
		EmployeePayrollData[] employeePayrollData = {null};
		try {
			connection[0] = EmployeePayrollDBService.getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			connection[0].setAutoCommit(false);
		} catch (SQLException e) {
			throw new PayrollServiceException(e.getMessage(), PayrollServiceException.ExceptionType.CONNECTION_PROBLEM);
		}
		
		 int employee_id = addEmployeeToPayrollDetail(connection[0],name,salary,startDate,gender);
	        boolean[] status = {false, false};

	        Runnable task1 = () -> {
	        	addEmployeePayroll(connection[0], employee_id, salary);
	            status[0] = true;
	        };
	        Thread thread1 = new Thread(task1);
	        thread1.start();
	    
	        while(status[0] == false || status[1] == false) {
	        	try {
	        		Thread.sleep(10);
	        	}catch (InterruptedException e) {
	        		System.out.println(e.getMessage());
	        	}
	        }

		try {
			  connection[0].commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection[0].close();
				} catch (SQLException e) {
					throw new PayrollServiceException(e.getMessage(),
							PayrollServiceException.ExceptionType.CONNECTION_PROBLEM);
				}
		}
		return employeePayrollData[0];
	}
	
	private void addEmployeePayroll(Connection connection, int employeeId, double salary) {
		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"insert into payroll_details (employee_id, basic_pay, deductions, taxable_pay, tax, net_pay) values "
							+ "('%s', '%s', '%s', '%s', '%s', '%s')",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
		}
		catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}
	}

	private int addEmployeeToPayrollDetail(Connection connection,String name, double salary, LocalDate startDate, String gender) throws PayrollServiceException {
		 int employeeId = -1;
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"insert into employee_payroll (name,gender,salary,start)" + "values ('%s', '%s', '%s', '%s')", name,
					gender, salary, Date.valueOf(startDate));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			return employeeId;
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new PayrollServiceException(e.getMessage(), PayrollServiceException.ExceptionType.INSERTION_PROBLEM);
		}
	}

	public int removeEmployee(String name) {
		try (Connection connection = EmployeePayrollDBService.getConnection();) {
			String sql = "update employee_payroll set is_active=? where name=?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setBoolean(1, false);
			preparedStatement.setString(2, name);
			int status = preparedStatement.executeUpdate();
			return status;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

}