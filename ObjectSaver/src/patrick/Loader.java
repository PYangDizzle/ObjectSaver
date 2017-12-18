package patrick;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class Loader {
	public static void main(String[] args)
			throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException, SecurityException {
		DUTObjectSaver dut = new DUTObjectSaver();

		Saver save = new Saver();
		String result = save.toJSON(dut);
		System.out.println(result);

		Loader load = new Loader();
		DUTObjectSaver copiedDUT = load.fromJSON(result, DUTObjectSaver.class);
		System.out.println();
	}

	private Map<Integer, Object> idObjectMap = new HashMap<>();

	public Object fromJSON(String json) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		StringReader string = new StringReader(json);
		BufferedReader reader = new BufferedReader(string);
		JsonParser rootParser = Json.createParser(reader);
		findAllObjects(rootParser);
		rootParser.close();

		string = new StringReader(json);
		reader = new BufferedReader(string);
		rootParser = Json.createParser(reader);
		populateAllFields(rootParser);
		rootParser.close();
		
		return idObjectMap.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T fromJSON(String json, Class<T> class1) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return (T)fromJSON(json);
	}

	private void populateAllFields(JsonParser parser) throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		String keyName = "";
		int id = -1;
		int length = -1;
		String structureName = "";
		String typeName = "";

		String fieldName = "";
		Object fieldValue = null;
		String fieldType = "";
		Class<?> fieldDeclaringClass = null;
		Object currObj = null;
		int index = -1;
		while (parser.hasNext()) {
			Event event = parser.next();

			switch (event) {
			case START_OBJECT:
				if (structureName.isEmpty()) {
					// beginning of all
				} else if (structureName.equals("Objects")) {
					// individual Object
					structureName = "Object";
				} else if (structureName.equals("Fields")) {
					// individual field
					structureName = "Field";
				}
				else if (structureName.equals("Elements")) {
					// individual element
					structureName = "Element";
				}
				break;

			case END_OBJECT:
				if (structureName.equals("Object")) {
					structureName = "Objects";
				} else if (structureName.equals("Field")) {
					structureName = "Fields";
				} else if (structureName.equals("Element")) {
					structureName = "Elements";
				}
				break;

			case KEY_NAME:
				keyName = parser.getString();
				if (keyName.equals("Objects") || keyName.equals("Fields") || keyName.equals("Elements")) {
					structureName = keyName;
				}
				break;
				
			case END_ARRAY:
				if (structureName.equals("Fields") || structureName.equals("Elements")) {
					structureName = "Object";
				}
				break;

			case VALUE_NUMBER:
				switch (keyName) {
				case "Index":
					index = parser.getInt();
					break;
				case "ID":
					id = parser.getInt();
					if (structureName.equals("Object")) {
						currObj = idObjectMap.get(id);
					} else if (structureName.equals("Field")) {
						fieldValue = idObjectMap.get(id);
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						field.set(currObj, fieldValue);
					}
					else if (structureName.equals("Element")) {
						fieldValue = idObjectMap.get(id);
						Array.set(currObj, index, fieldValue);
					}
					break;
				case "Length":
					length = parser.getInt();
					break;
				case "Value":
					if (structureName.equals("Field")) {
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						switch (fieldType) {
						case "int":
							field.setInt(currObj, parser.getInt());
							break;
						case "long":
							field.setLong(currObj, parser.getLong());
							break;
						case "short":
							field.setShort(currObj, (short) parser.getInt());
							break;
						case "byte":
							field.setByte(currObj, (byte) parser.getInt());
							break;
						// represented as string instead of int
						// case "char":
						// field.setChar(currObj, );
						// break;
						case "double":
							field.setDouble(currObj, parser.getBigDecimal().doubleValue());
							break;
						case "float":
							field.setFloat(currObj, parser.getBigDecimal().floatValue());
							break;
						// VALUE_TRUE or VALUE_FALSE
						// case "boolean":
						// field.set(currObj, parser.getLong());
						// break;
						}
					}
					else if (structureName.equals("Element")) {
						switch (fieldType) {
						case "int":
							Array.setInt(currObj, index, parser.getInt());
							break;
						case "long":
							Array.setLong(currObj, index, parser.getLong());
							break;
						case "short":
							Array.setShort(currObj, index, (short) parser.getInt());
							break;
						case "byte":
							Array.setByte(currObj, index, (byte) parser.getInt());
							break;
						// represented as string instead of int
						// case "char":
						// Array.setChar(currObj, index, );
						// break;
						case "double":
							Array.setDouble(currObj, index, parser.getBigDecimal().doubleValue());
							break;
						case "float":
							Array.setFloat(currObj, index, parser.getBigDecimal().floatValue());
							break;
						// VALUE_TRUE or VALUE_FALSE
						// case "boolean":
						// field.set(currObj, parser.getLong());
						// break;
						}
					}
					break;
				}
				break;

			case VALUE_STRING:
				switch (keyName) {
				case "Declared Type":
					fieldType = parser.getString();
					break;

				case "Declaring Class":
					fieldDeclaringClass = Class.forName(parser.getString());
					break;

				case "Name":
					fieldName = parser.getString();
					break;

				case "Value":
					if (structureName.equals("Field")) {
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						// represented as string instead of int
						field.setChar(currObj, parser.getString().charAt(0));
					}
					else if (structureName.equals("Element")) {
						Array.setChar(currObj, index, parser.getString().charAt(0));
					}
					break;
				}

				break;
			case VALUE_TRUE:
				switch (keyName) {
				case "Value":
					if (structureName.equals("Field")) {
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						field.setBoolean(currObj, true);
						break;
					}
					else if (structureName.equals("Element")) {
						Array.setBoolean(currObj, index, true);
					}
					break;
				}
				break;
			case VALUE_FALSE:
				switch (keyName) {
				case "Value":
					if (structureName.equals("Field")) {
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						field.setBoolean(currObj, false);
						break;
					}
					else if (structureName.equals("Element")) {
						Array.setBoolean(currObj, index, false);
					}
					break;
				}
				break;
			case VALUE_NULL:
				switch (keyName) {
				case "Value":
					if (structureName.equals("Field")) {
						Field field = fieldDeclaringClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						field.set(currObj, null);
					}
					else if (structureName.equals("Element")) {
						Array.set(currObj, index, null);
					}
					break;
				}
				break;
			default:
				break;
			}
		}
	}

	private void findAllObjects(JsonParser parser) throws ClassNotFoundException {
		String keyName = "";
		int id = -1;
		int length = -1;
		String structureName = "";
		String typeName = "";
		boolean continueToNextObject = false;
		while (parser.hasNext()) {
			Event event = parser.next();

			if (continueToNextObject && !event.equals(Event.END_OBJECT) && structureName.equals("Object")) {
				continue;
			}

			switch (event) {
			case START_OBJECT:
				if (structureName.isEmpty()) {
					// beginning of all
				} else if (structureName.equals("Objects")) {
					// individual Object
					structureName = "Object";
				}
				break;

			case END_OBJECT:
				if (structureName.equals("Object")) {
					structureName = "Objects";
					continueToNextObject = false;
				}
				break;

			case KEY_NAME:
				keyName = parser.getString();
				if (structureName.isEmpty() && keyName.equals("Objects")) {
					structureName = keyName;
				}
				break;

			case VALUE_NUMBER:
				switch (keyName) {
				case "ID":
					id = parser.getInt();
					break;

				case "Length":
					length = parser.getInt();
					break;
				}
				break;

			case VALUE_STRING:
				switch (keyName) {
				case "Type Name":
					typeName = parser.getString();
					break;
				case "Type":
					String test = parser.getString();
					try {
						
						Class<?> type = Class.forName(test);
					
					Object obj;
					if (type.equals(Class.class)) {
						obj = ReflectionUtil.INSTANCE.getClass(typeName);
					} else if (type.isArray()) {
						obj = Array.newInstance(type.getComponentType(), length);
					} else {
						obj = UnsafeUtil.INSTANCE.allocateInstance(type);
					}
					idObjectMap.put(id, obj);
					continueToNextObject = true;
					}
					catch (Exception e) {
						System.out.println();
					}
					break;
				}
				break;

			default:
				break;
			}
		}
	}
}
