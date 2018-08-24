package org.simplexml.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.simplexml.XMLSerializer;
import org.xml.sax.SAXException;

public class XMLSerializerTest {
	private String xmlString;
	private Person person;

	@Before
	public void setUp() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1854);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 6);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date date = cal.getTime();
		long millis = cal.getTime().getTime();

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n");
		sb.append("<Person>\r\n");
		sb.append("  <FirstName>Sherlock</FirstName>\r\n");
		sb.append("  <LastName>Holmes</LastName>\r\n");
		sb.append("  <Gender>MALE</Gender>\r\n");
		sb.append("  <Age>164</Age>\r\n");
		sb.append("  <BirthDate>" + millis + "</BirthDate>\r\n");
		sb.append("  <IsDetective>true</IsDetective>\r\n");
		sb.append("  <Address>\r\n");
		sb.append("    <Street>221B Baker Street</Street>\r\n");
		sb.append("    <City>London</City>\r\n");
		sb.append("  </Address>\r\n");
		sb.append("  <Books>\r\n");
		sb.append("    <Book>\r\n");
		sb.append("      <Title>The Hound of the Baskervilles</Title>\r\n");
		sb.append("    </Book>\r\n");
		sb.append("    <Book>\r\n");
		sb.append("      <Title>The Sign of Four</Title>\r\n");
		sb.append("    </Book>\r\n");
		sb.append("  </Books>\r\n");
		sb.append("</Person>\r\n");
		xmlString = sb.toString();

		person = new Person();
		person.setId(2018);
		person.setFirstName("Sherlock");
		person.setLastName("Holmes");
		person.setGender(Gender.MALE);
		person.setAge(164);
		person.setBirthDate(date);
		person.setIsDetective(true);

		Address address = new Address();
		address.setStreet("221B Baker Street");
		address.setCity("London");
		person.setAddress(address);

		BookList books = new BookList();
		Book book1 = new Book();
		book1.setTitle("The Hound of the Baskervilles");
		books.add(book1);
		Book book2 = new Book();
		book2.setTitle("The Sign of Four");
		books.add(book2);
		person.setBooks(books);
	}

	@Test
	public void testSerialization() throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try {
				XMLSerializer.serialize(baos, person);
				String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);

				Assert.assertEquals(xmlString, result);
			} catch (Exception e) {
				Assert.fail(e.toString());
			}
		}
	}

	@Test
	public void testDeserialization() throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
			try {
				Person result = (Person) XMLSerializer.deserialize(bais, Person.class);

				Assert.assertEquals(person, result);
				Assert.assertNotEquals(person.getId(), result.getId());
			} catch (Exception e) {
				Assert.fail(e.toString());
			}
		}
	}

	@Test
	public void testDeserializationWithWrongRootNode() throws IOException {
		String wrongXmlString = xmlString.replaceAll("Person", "P");

		try (ByteArrayInputStream bais = new ByteArrayInputStream(wrongXmlString.getBytes(StandardCharsets.UTF_8))) {
			Throwable t = null;

			try {
				XMLSerializer.deserialize(bais, Person.class);
			} catch (Exception e) {
				t = e;
			}

			Assert.assertNotNull(t);
			Assert.assertTrue(t instanceof SAXException);
		}
	}

	@Test
	public void testDeserializationWithMissingElements() throws IOException {
		String modifiedXmlString = xmlString.replaceFirst("  <FirstName>Sherlock</FirstName>\r\n", "");

		try (ByteArrayInputStream bais = new ByteArrayInputStream(modifiedXmlString.getBytes(StandardCharsets.UTF_8))) {
			try {
				Person result = (Person) XMLSerializer.deserialize(bais, Person.class);

				Assert.assertNotEquals(person, result);
				Assert.assertEquals("", result.getFirstName());
				Assert.assertEquals("Holmes", result.getLastName());
			} catch (Exception e) {
				Assert.fail(e.toString());
			}
		}
	}

	@Test
	public void testDeserializationWithInvalidValues() throws IOException {
		String wrongXmlString = xmlString.replaceFirst("<Age>164</Age>", "<Age>old</Age>")
				.replaceFirst("<Gender>MALE</Gender>", "<Gender>UNKNOWN</Gender>");

		try (ByteArrayInputStream bais = new ByteArrayInputStream(wrongXmlString.getBytes(StandardCharsets.UTF_8))) {
			Throwable t = null;

			try {
				XMLSerializer.deserialize(bais, Person.class);
			} catch (Exception e) {
				t = e;
			}

			Assert.assertNotNull(t);
			Assert.assertTrue(t instanceof InvocationTargetException);
		}
	}
}