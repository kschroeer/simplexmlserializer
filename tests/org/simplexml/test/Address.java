package org.simplexml.test;

public class Address {
	private String street;
	private String city;

	public Address() {
		street = "";
		city = "";
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Address)) {
			return false;
		}

		Address anotherAddress = (Address) obj;
		return this.getStreet().equals(anotherAddress.getStreet()) && this.getCity().equals(anotherAddress.getCity());
	}
}