package patrick;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.json.JsonValue;

public enum ReflectionUtil {
	INSTANCE;
	
	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field[] fields = INSTANCE.getDeclaredFieldsAlsoFromSuper(DuplicatePublicFieldInObjectAndSuper.class);
		System.out.println(Arrays.toString(fields));
		DuplicatePublicFieldInObjectAndSuper dut = new DuplicatePublicFieldInObjectAndSuper();
		System.out.println(dut.test);
		System.out.println(((Super)dut).test);
		
		Field field = Super.class.getField("test");
		field.setAccessible(true);
		field.set(dut, 111);
		System.out.println(dut.test);
		System.out.println(((Super)dut).test);
	}
	
	public Field[] getDeclaredFieldsAlsoFromSuper(Class<?> type) {
		List<Field> fields = new LinkedList<>();
		while(type != null) {
			fields.addAll(Arrays.asList(type.getDeclaredFields()));
			type = type.getSuperclass();
		}
		// Why ClassCastException?
//		return (Field[])fields.toArray();
		return fields.toArray(new Field[0]);
	}
	
//	public void walk(FieldVisitor visitor, Field... fields) {
//		for (Field field : fields) {
//			visitor.visit(field);
//		}
//	}
	
	public Class<?> getClass(String className) throws ClassNotFoundException{
	  if("int" .equals(className)) 		return int		.class;
	  if("long".equals(className)) 		return long		.class;
	  if("short" .equals(className)) 	return short	.class;
	  if("byte".equals(className)) 		return byte		.class;
	  if("char" .equals(className)) 	return char		.class;
	  if("float".equals(className)) 	return float	.class;
	  if("double" .equals(className)) 	return double	.class;
	  if("boolean".equals(className)) 	return boolean	.class;
	  return Class.forName(className);
	}
}
