package com.blz.InsertDetails;

import java.time.LocalDate;
import java.util.Objects;

public class EmployeePayrollData {

	public int id;
	public String name;
	public double salary;
	public LocalDate start;
	private String[] dept_name;
	private char gender;
	public String gender1;

	/* Constructor */
	public EmployeePayrollData(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public EmployeePayrollData(int id, String name, double salary, LocalDate startDate, char gender,
			String[] dept_name) {
		this(id, name, salary, startDate);
		this.gender = gender;
		this.dept_name = dept_name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, salary, start, gender1);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmployeePayrollData other = (EmployeePayrollData) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(salary) != Double.doubleToLongBits(other.salary))
			return false;
		if  (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	/* Constructor */
	public EmployeePayrollData(int id, String name, double salary, LocalDate start) {
		this(id, name, salary);
		this.start = start;
	}

	public EmployeePayrollData(int id, String name, double salary, LocalDate start, String gender1) {
		this(id, name, salary, start);
		this.gender1 = gender1;
	}

	@Override
	public String toString() {
		return "EmployeePayrollData [ID=" + id + ", Name=" + name + ", Salary=" + salary + ", Start=" + start + "]";
	}

}
