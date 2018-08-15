package org.simplexml;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputValidation;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class provides methods to store Java objcts in a xml file or load a xml
 * file and map its content back to a Java object.
 *
 * @author <a href="mailto:acsf.dev@gmail.com">Kay Schröer</a>
 */
public final class XMLSerializer {
	private static Document doc;

	private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;

	static {
		PRIMITIVES_TO_WRAPPERS = new HashMap<>();
		PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
		PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
		PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
		PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
		PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
		PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
		PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
		PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
	}

	private XMLSerializer() {
	}

	/**
	 * Deserializes from a file.
	 *
	 * @param file
	 *            path and name of a xml file
	 * @param clazz
	 *            expected type
	 *
	 * @return deserialized object
	 *
	 * @throws Exception
	 */
	public static Object deserialize(File file, Class<?> clazz) throws Exception {
		doc = XMLHelper.parse(file);
		return deserializeThat(clazz);
	}

	/**
	 * Deserializes from a stream.
	 *
	 * @param inputStream
	 *            data stream containing the xml structure
	 * @param clazz
	 *            expected type
	 *
	 * @return deserialized object
	 *
	 * @throws Exception
	 */
	public static Object deserialize(InputStream inputStream, Class<?> clazz) throws Exception {
		doc = XMLHelper.parse(inputStream);
		return deserializeThat(clazz);
	}

	/**
	 * Serializes to a file.
	 *
	 * @param file
	 *            path and name of the xml file
	 * @param obj
	 *            any object to serialize
	 *
	 * @throws Exception
	 */
	public static void serialize(File file, Object obj) throws Exception {
		serializeThat(obj);
		XMLHelper.write(file, doc);
	}

	/**
	 * Serializes to a stream.
	 *
	 * @param outputStream
	 *            data stream to store the xml structure
	 * @param obj
	 *            any object to serialize
	 *
	 * @throws Exception
	 */
	public static void serialize(OutputStream outputStream, Object obj) throws Exception {
		serializeThat(obj);
		XMLHelper.write(outputStream, doc);
	}

	private static Object deserializeThat(Class<?> clazz) throws Exception {
		Element rootNode = doc.getDocumentElement();
		String rootNodeName = clazz.getPackage().getName() + "." + rootNode.getNodeName();
		if (!rootNodeName.equals(clazz.getName())) {
			throw new SAXException("Wrong root node.");
		}

		Object obj = resolveDomTree(clazz, rootNode);
		return obj;
	}

	private static void serializeThat(Object obj) throws Exception {
		doc = XMLHelper.createEmptyDocument();
		Element rootNode = doc.createElement(obj.getClass().getSimpleName());
		doc.appendChild(rootNode);
		buildDomTree(obj, rootNode);
	}

	private static void buildDomTree(Object obj, Node parentNode) throws Exception {
		if (obj == null) {
			return;
		}

		Class<?> memberType = obj.getClass();
		TypeKind typeKind = TypeKind.valueOf(memberType);

		if (typeKind.isObject()) {
			List<Field> allFields = new ArrayList<>(Arrays.asList(obj.getClass().getDeclaredFields()));

			for (Field field : allFields) {
				field.setAccessible(true);

				if (!Modifier.isTransient(field.getModifiers())) {
					Element childNode = doc.createElement(toUCFirst(field.getName()));
					parentNode.appendChild(childNode);

					Object childObj = field.get(obj);
					buildDomTree(childObj, childNode);
				}
			}
		} else if (typeKind.isArray()) {
			int length = Array.getLength(obj);

			for (int i = 0; i < length; i++) {
				Object listObj = Array.get(obj, i);
				Element listNode = doc.createElement(listObj.getClass().getSimpleName());
				parentNode.appendChild(listNode);
				buildDomTree(listObj, listNode);
			}
		} else if (typeKind.isCollection()) {
			Collection<?> list = (Collection<?>) obj;

			for (Object listObj : list) {
				Element listNode = doc.createElement(listObj.getClass().getSimpleName());
				parentNode.appendChild(listNode);
				buildDomTree(listObj, listNode);
			}
		} else if (typeKind.isMap()) {
			Map<?, ?> map = (Map<?, ?>) obj;

			for (Object keyObj : map.keySet()) {
				Element keyNode = doc.createElement(keyObj.getClass().getSimpleName());
				parentNode.appendChild(keyNode);
				buildDomTree(keyObj, keyNode);

				Object valueObj = map.get(keyObj);
				Element valueNode = doc.createElement(valueObj.getClass().getSimpleName());
				parentNode.appendChild(valueNode);
				buildDomTree(valueObj, valueNode);
			}
		} else {
			Text newText = doc.createTextNode(makeString(obj));
			parentNode.appendChild(newText);
		}
	}

	private static Class<?>[] getTypeArguments(Class<?> clazz) {
		Type superType = clazz;

		do {
			superType = ((Class<?>) superType).getGenericSuperclass();
			if (superType instanceof ParameterizedType) {
				Type[] typeArgs = ((ParameterizedType) superType).getActualTypeArguments();
				Class<?>[] result = new Class<?>[typeArgs.length];

				for (int i = 0; i < typeArgs.length; i++) {
					result[i] = (Class<?>) typeArgs[i];
				}

				return result;
			}
		} while (!superType.equals(Object.class));

		return null;
	}

	private static Object makeObject(String str, Class<?> clazz) throws Exception {
		if (clazz.isPrimitive()) {
			clazz = PRIMITIVES_TO_WRAPPERS.get(clazz);
		}

		if (clazz == Character.class) {
			return Character.valueOf(str.charAt(0));
		} else if (clazz == Date.class) {
			return new Date(Long.parseLong(str));
		} else if (clazz == File.class) {
			return new File(str);
		} else {
			try {
				Method initMethod = clazz.getMethod("valueOf", String.class);
				return initMethod.invoke(null, str);
			} catch (NoSuchMethodException e) {
				return str;
			}
		}
	}

	private static String makeString(Object obj) {
		if (obj instanceof Date) {
			return Long.toString(((Date) obj).getTime());
		} else {
			return obj.toString();
		}
	}

	private static Object resolveDomTree(Class<?> memberType, Node parentNode) throws Exception {
		TypeKind typeKind = TypeKind.valueOf(memberType);

		if (typeKind.isObject()) {
			List<Field> allFields = new ArrayList<>(Arrays.asList(memberType.getDeclaredFields()));
			Object obj = memberType.newInstance();

			for (Field field : allFields) {
				field.setAccessible(true);

				if (!Modifier.isTransient(field.getModifiers())) {
					NodeList children = XMLHelper.getNodeList(parentNode, toUCFirst(field.getName()));

					if (children.getLength() == 1) {
						Node childNode = children.item(0);
						Object childObj = resolveDomTree(field.getType(), childNode);

						if (childObj != null) {
							field.set(obj, childObj);
						}
					}
				}
			}

			if (obj instanceof ObjectInputValidation) {
				((ObjectInputValidation) obj).validateObject();
			}

			return obj;
		} else if (typeKind.isArray()) {
			Class<?> itemClass = memberType.getComponentType();
			Class<?> queryClass = itemClass;

			if (queryClass.isPrimitive()) {
				queryClass = PRIMITIVES_TO_WRAPPERS.get(queryClass);
			}

			NodeList children = XMLHelper.getNodeList(parentNode, queryClass.getSimpleName());
			Object newList = Array.newInstance(itemClass, children.getLength());

			for (int i = 0; i < children.getLength(); i++) {
				Node listNode = children.item(i);
				Object listObj = resolveDomTree(itemClass, listNode);

				if (listObj != null) {
					Array.set(newList, i, listObj);
				}
			}

			return newList;
		} else if (typeKind.isCollection()) {
			Class<?>[] itemClass = getTypeArguments(memberType);
			NodeList children = XMLHelper.getNodeList(parentNode, itemClass[0].getSimpleName());
			Collection<?> newList = (Collection<?>) memberType.newInstance();
			Method addMethod = memberType.getMethod("add", Object.class);

			for (int i = 0; i < children.getLength(); i++) {
				Node listNode = children.item(i);
				Object listObj = resolveDomTree(itemClass[0], listNode);

				if (listObj != null) {
					addMethod.invoke(newList, listObj);
				}
			}

			return newList;
		} else if (typeKind.isMap()) {
			Class<?>[] itemClass = getTypeArguments(memberType);
			NodeList children = XMLHelper.getNodeList(parentNode, "*[name()='" + itemClass[0].getSimpleName()
					+ "' or name()='" + itemClass[1].getSimpleName() + "']");
			Map<?, ?> newMap = (Map<?, ?>) memberType.newInstance();
			Method putMethod = memberType.getMethod("put", Object.class, Object.class);

			if (children.getLength() % 2 == 0) {
				for (int i = 0; i < children.getLength(); i += 2) {
					Node keyNode = children.item(i);
					Node valueNode = children.item(i + 1);

					if (keyNode.getNodeName().equals(itemClass[0].getSimpleName())
							&& valueNode.getNodeName().equals(itemClass[1].getSimpleName())) {
						Object keyObj = resolveDomTree(itemClass[0], keyNode);
						Object valueObj = resolveDomTree(itemClass[1], valueNode);

						if (keyObj != null && valueObj != null) {
							putMethod.invoke(newMap, keyObj, valueObj);
						}
					}
				}
			}

			return newMap;
		} else {
			String textContent = "";
			if (parentNode.hasChildNodes()) {
				textContent = parentNode.getFirstChild().getTextContent();
			}

			Object obj = makeObject(textContent, memberType);
			return obj;
		}
	}

	private static String toUCFirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private static enum TypeKind {
		ARRAY, COLLECTION, MAP, OBJECT, TEXT;

		public boolean isArray() {
			return this == ARRAY;
		}

		public boolean isCollection() {
			return this == COLLECTION;
		}

		public boolean isMap() {
			return this == MAP;
		}

		public boolean isObject() {
			return this == OBJECT;
		}

		public static TypeKind valueOf(Class<?> clazz) {
			for (Class<?> wrapperClass : PRIMITIVES_TO_WRAPPERS.values()) {
				if (clazz.equals(wrapperClass)) {
					return TEXT;
				}
			}

			if (clazz.isPrimitive() || clazz.equals(Date.class) || clazz.equals(File.class)
					|| clazz.equals(String.class)) {
				return TEXT;
			} else if (clazz.isArray()) {
				return ARRAY;
			} else {
				while (!clazz.equals(Object.class)) {
					if (clazz.equals(Enum.class)) {
						return TEXT;
					} else if (hasInterface(clazz, Collection.class)) {
						return COLLECTION;
					} else if (hasInterface(clazz, Map.class)) {
						return MAP;
					}
					clazz = clazz.getSuperclass();
				}
				return OBJECT;
			}
		}

		private static boolean hasInterface(Class<?> clazz, Class<?> search) {
			for (Class<?> aInterface : clazz.getInterfaces()) {
				if (aInterface.equals(search)) {
					return true;
				}
			}
			return false;
		}
	}
}