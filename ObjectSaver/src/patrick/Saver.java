package patrick;

import static patrick.JSONUtil.pretty;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class Saver {
	
	// Test Purpose
	public Object caller;
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, Exception {
		
		DUTObjectSaver dut = new DUTObjectSaver();
		
		Saver save = new Saver();
		save.setVerbose(true);
		String result = save.toJSON(dut);
		System.out.println(result);
		
//		Object type = DUTObjectSaver.class;
//		System.out.println(type.getClass().getName());
//		System.out.println(type.getClass().toString());
//		System.out.println(type.getClass().toGenericString());
	}
	
	public void setVerbose(boolean truth) {
		uow.verbose = truth;
	}

	private List<UniqueObjectWrapper> cachedUOWs = new LinkedList<>();
	private UniqueObjectWrapper uow = new UniqueObjectWrapper();

	public String toJSON(Object obj) throws IllegalArgumentException, IllegalAccessException {
		// root has all the objects
		// JSONArray root = Factory.INSTANCE.get(JSONArray.class);
		JsonArrayBuilder rootBuilder = Json.createArrayBuilder();
		UniqueObjectWrapper uniqueObj = uow.wrap(obj);
		cachedUOWs.add(uniqueObj);
		findAllObjects(uniqueObj, rootBuilder);
		JsonObjectBuilder objBuilder = Json.createObjectBuilder().add("Objects", rootBuilder);
		return pretty(objBuilder.build());
	}

	private void findAllObjects(UniqueObjectWrapper uowObj, JsonArrayBuilder rootBuilder)
			throws IllegalArgumentException, IllegalAccessException {
		// insertObject(rootBuilder, obj);
		List<UniqueObjectWrapper> objsToRecurse = new LinkedList<>();
		JsonArrayBuilder fieldsBuilder = Json.createArrayBuilder();
		Object obj = uowObj.obj;
		if (obj instanceof Class) {
			// do nothing
			// Class is a singleton in JVM, InstantiationException occurs
		}
		else if (obj.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(obj); i++) {
				addElement(fieldsBuilder, i, obj, objsToRecurse);
			}
		}
		else {
			Field[] fields = ReflectionUtil.INSTANCE.getDeclaredFieldsAlsoFromSuper(obj.getClass());
			Field.setAccessible(fields, true);
			
			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					addField(fieldsBuilder, field, obj, objsToRecurse);
				}
			}
		}

		insertObject(rootBuilder, uowObj, fieldsBuilder);

		for (UniqueObjectWrapper objToRecurse : objsToRecurse) {
			findAllObjects(objToRecurse, rootBuilder);
		}
	}
	
	private void addElement(JsonArrayBuilder fieldsBuilder, int index, Object obj,
			List<UniqueObjectWrapper> objsToRecurse) throws IllegalArgumentException, IllegalAccessException {
		addFieldOrElement(true, fieldsBuilder, null, index, obj, objsToRecurse);
	}
	
	private void addField(JsonArrayBuilder fieldsBuilder, Field field, Object obj,
			List<UniqueObjectWrapper> objsToRecurse) throws IllegalArgumentException, IllegalAccessException {
		addFieldOrElement(false, fieldsBuilder, field, -1, obj, objsToRecurse);
	}

	/**
	 * @param isArray
	 * - If isArray == true, using 'field' is results in undefined behavior and vice versa
	 * @param fieldsBuilder
	 * @param field
	 * @param index
	 * @param obj
	 * @param objsToRecurse
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws Exception
	 */
	private void addFieldOrElement(boolean isArray, JsonArrayBuilder fieldsBuilder, Field field, int index, Object obj,
			List<UniqueObjectWrapper> objsToRecurse) throws IllegalArgumentException, IllegalAccessException {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder();
		if (isArray) {
			// Do nothing
		}
		else {
			startFieldValueJSON(fieldBuilder, field);
		}
		if (isArray) {
			fieldBuilder.add("Index", index);
		}
		else {
			fieldBuilder.add("Name", field.getName());
		}
		Class<?> type;
		if (isArray) {
			type = obj.getClass().getComponentType();
		}
		else {
			type = field.getType();
		}
		if (type.isPrimitive()) {
			// primitives are written directly
			if (isArray) {
				addPrimitiveElement(fieldBuilder, index, obj);
			}
			else {
				addPrimitiveField(fieldBuilder, field, obj);
			}
		} 
		else {
			Object fieldObj;
			if (isArray) {
				fieldObj = Array.get(obj, index);
			}
			else {
				fieldObj = field.get(obj);
			}

			if (fieldObj != null) {
				if (type.equals(Class.class)) {
					fieldBuilder.add("Type Name", ((Class<?>)field.get(obj)).getName());
				}
				int id = -1;
				SEARCH_CACHE: {
					for (UniqueObjectWrapper cachedUOW : cachedUOWs) {
						if (cachedUOW.equals(fieldObj)) {
							id = cachedUOW.id;
							fieldBuilder.add("ID", cachedUOW.id);
							break SEARCH_CACHE;
						}
					}

					UniqueObjectWrapper fieldObjWrapped = uow.wrap(fieldObj);
					objsToRecurse.add(fieldObjWrapped);
					id = fieldObjWrapped.id;
					fieldBuilder.add("ID", id);
					cachedUOWs.add(fieldObjWrapped);
				}
				
				if (fieldObj instanceof String) {
					fieldBuilder.add("Literal", fieldObj.toString());
				}
			} else {
				fieldBuilder.add("Value", JsonValue.NULL);
			}
		}
		
		fieldsBuilder.add(fieldBuilder);
	}

	private void startFieldValueJSON(JsonObjectBuilder fieldBuilder, Field field) {
		fieldBuilder
				// is Declared Type needed? Yes, while parsing value
				.add("Declared Type", field.getType().getName())
				.add("Declaring Class", field.getDeclaringClass().getName());
	}

	private void addPrimitiveField(JsonObjectBuilder fieldsBuilder, Field field, Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		String name = "Value";
		if (int.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getInt(obj));
		} else if (long.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getLong(obj));
		} else if (short.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getShort(obj));
		} else if (byte.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getByte(obj));
		} else if (char.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getChar(obj));
		} else if (double.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getDouble(obj));
		} else if (float.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getFloat(obj));
		} else if (boolean.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, field.getBoolean(obj));
		} else {
			throw new IllegalArgumentException("Not Primitive: " + field);
		}
	}
	
	private void addPrimitiveElement(JsonObjectBuilder fieldsBuilder, int index, Object obj) {
		Class<?> type = obj.getClass().getComponentType();
		String name = "Value";
		if (int.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getInt(obj, index));
		} else if (long.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getLong(obj, index));
		} else if (short.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getShort(obj, index));
		} else if (byte.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getByte(obj, index));
		} else if (char.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, String.valueOf(Array.getChar(obj, index)));
		} else if (double.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getDouble(obj, index));
		} else if (float.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getFloat(obj, index));
		} else if (boolean.class.isAssignableFrom(type)) {
			fieldsBuilder.add(name, Array.getBoolean(obj, index));
		} else {
			throw new IllegalArgumentException("Not Primitive: " + obj + " at index: " + index);
		}
	}

	private void insertObject(JsonArrayBuilder rootBuilder, UniqueObjectWrapper obj, JsonArrayBuilder fields) {
		// try {
//		Object content = obj.obj;
//		ClassLoader systemCL = ClassLoader.getSystemClassLoader();
//		ClassLoader thisCL = this.getClass().getClassLoader();
//		ClassLoader callerCL = caller.getClass().getClassLoader();
//		ClassLoader objCL = obj.obj.getClass().getClassLoader();
//		
		JsonObjectBuilder jsonObj = Json.createObjectBuilder().add("ID", obj.id);
				
		if (obj.obj.getClass().isArray()) {
			jsonObj.add("Length", Array.getLength(obj.obj));
		}
		else if (obj.obj instanceof Class) {
			jsonObj.add("Type Name", ((Class)obj.obj).getName());
		}
		else if (obj.obj instanceof String) {
			jsonObj.add("Literal", obj.obj.toString());
		}
				
		jsonObj.add("Type", obj.obj.getClass().getName());
		
		if (!(obj.obj instanceof Class)) {
			jsonObj.add(obj.obj.getClass().isArray() ? "Elements" : "Fields", fields);
		}
		
		rootBuilder.add(jsonObj);
		// }
		// catch(Exception e) {
		// System.out.println();
		// }
	}
}
