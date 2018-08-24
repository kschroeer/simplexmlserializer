package org.simplexml.test;

public class Book {
	private String title;

	public Book() {
		title = "";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Book)) {
			return false;
		}

		Book anotherBook = (Book) obj;
		return this.getTitle().equals(anotherBook.getTitle());
	}
}