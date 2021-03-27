package com.blz.InsertDetails;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeePayrollService {
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private EmployeePayrollDBService employeePayrollDBService;
	private  EmployeePayrollDBServiceERD employeePayrollDBServiceERD;

	/* Welcome Message */
	public void printWelcomeMessage() {
		System.out.println("Welcome to the Employee PayRoll Service Program");
	}

	private static List<EmployeePayrollData> employeePayrollList;

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
		employeePayrollDBServiceERD = EmployeePayrollDBServiceERD.getInstance();
	}

	public static void main(String[] args) {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}

	/* Read Employee Payroll data from console */
	public void readEmployeePayrollData(Scanner consoleInputReader) {
		System.out.println("Enter Employee ID: ");
		int id = consoleInputReader.nextInt();
		System.out.println("Enter Employee Name ");
		String name = consoleInputReader.next();
		System.out.println("Enter Employee Salary ");
		double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	/* Adding Employee Payroll using multithreads */
	public void addEmployeesToPayrollWithThreads(List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				System.out.println("Employee Being Added: " + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
							employeePayrollData.start, employeePayrollData.gender1);
				} catch (PayrollServiceException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				System.out.println("Employee Added " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while(employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(employeePayrollDataList);

	}

	public void addEmployeesToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			System.out.println("Employee Being Added: " + employeePayrollData.name);
			try {
				this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
						employeePayrollData.start, employeePayrollData.gender1);
				System.out.println("Employee Added:" + employeePayrollData.name);
			} catch (PayrollServiceException e) {
				e.printStackTrace();
			}
			System.out.println(this.employeePayrollList);
		});

	}

	public void addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender)
			throws PayrollServiceException {
		employeePayrollList.add(employeePayrollDBServiceERD.addEmployeeToPayroll(name, salary, startDate, gender));
	}

	/* Write Employee Payroll data to console */
	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("\nWriting Employee Payroll Roaster to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new EmployeePayrollFileIOService().writeData(employeePayrollList);
		}
	}

	/* Print Employee Payroll */
	public void printData(IOService fileIo) {
		if (fileIo.equals(IOService.FILE_IO)) {
			new EmployeePayrollFileIOService().printData();
		}
	}

	public long countEntries(IOService fileIo) {
		if (fileIo.equals(IOService.FILE_IO)) {
			return new EmployeePayrollFileIOService().countEntries();
		}
		return employeePayrollList.size();
	}

	public List<EmployeePayrollData> readPayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		return employeePayrollList;
	}

	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService) throws PayrollServiceException {
		employeePayrollList = null;
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readData();
		return employeePayrollList;
	}

	public void updateEmployeeSalary(String name, double salary) throws PayrollServiceException {
		int result = employeePayrollDBService.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;

	}

	private EmployeePayrollData getEmployeePayrollData(String name) {
		EmployeePayrollData employeePayrollData;
		employeePayrollData = this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name)).findFirst().orElse(null);
		return employeePayrollData;
	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));

	}

	public List<EmployeePayrollData> readEmployeePayrollForDateRange(IOService ioService, LocalDate startDate,
			LocalDate endDate) throws PayrollServiceException {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getEmployeeForDateRange(startDate, endDate);
		return null;
	}

	public Map<String, Double> readAverageSalaryByGender(IOService ioService) throws PayrollServiceException {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getAverageSalaryByGender();
		return null;
	}

	public Map<String, Double> readCountByGender(IOService ioService) throws PayrollServiceException {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getCountByGender();
		return null;
	}

	public Map<String, Double> readMinumumSalaryByGender(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getMinimumByGender();
		return null;
	}

	public Map<String, Double> readMaximumSalaryByGender(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getMaximumByGender();
		return null;
	}

	public Map<String, Double> readSumSalaryByGender(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getSalarySumByGender();
		return null;
	}

	public int removeEmployeeFromPayroll(String name, IOService ioService) {
		int employeeCount = 0;
		if (ioService.equals(IOService.DB_IO))
			employeeCount = employeePayrollDBServiceERD.removeEmployee(name);
		return employeeCount;
	}

	/*
	 * public List<EmployeePayrollData> readActiveEmployeePayrollData(IOService
	 * ioService) { if (ioService.equals(IOService.DB_IO)) this.employeePayrollList
	 * = employeePayrollDBService.readActiveEmployeeData(); return
	 * this.employeePayrollList; }
	 */
}