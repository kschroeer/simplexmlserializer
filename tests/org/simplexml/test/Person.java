package org.simplexml.test;

import java.util.Date;

public class Person {
	private transient int id;
	private String firstName;
	private String lastName;
	private Gender gender;
	private int age;
	private Date birthDate;
	private boolean isDetective;
	private Address address;
	private BookList books;

	public Person() {
		id = 1;
		firstName = "";
		lastName = "";
		gender = Gender.NONE;
		age = 0;
		birthDate = new Date();
		isDetective = false;
		address = new Address();
		books = new BookList();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public boolean isDetective() {
		return isDetective;
	}

	public void setIsDetective(boolean isDetective) {
		this.isDetective = isDetective;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public BookList getBooks() {
		return books;
	}

	public void setBooks(BookList books) {
		this.books = books;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Person)) {
			return false;
		}

		Person anotherPerson = (Person) obj;
		return this.getFirstName().equals(anotherPerson.getFirstName())
				&& this.getLastName().equals(anotherPerson.getLastName())
				&& this.getGender() == anotherPerson.getGender() && this.getAge() == anotherPerson.getAge()
				&& this.getBirthDate().equals(anotherPerson.getBirthDate())
				&& this.isDetective() == anotherPerson.isDetective()
				&& this.getAddress().equals(anotherPerson.getAddress())
				&& this.getBooks().containsAll(anotherPerson.getBooks());
	}
}