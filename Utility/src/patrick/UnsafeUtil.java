package patrick;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sun.misc.Unsafe;

public enum UnsafeUtil {
	INSTANCE;
	
	public boolean saferMode = false;
	
	private Unsafe unsafe;
	
	UnsafeUtil() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			this.unsafe = (Unsafe)field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T allocateInstance(Class<T> type) {
		if (saferMode) {
			if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
				return null;
			}
			try {
				// try if no-args constructor exists
				Constructor<T> constr = type.getDeclaredConstructor();
				constr.setAccessible(true);
				return constr.newInstance();
			}
			catch (Exception e) {
				// do nothing
				// default back to unsafe
			}
		}
		try {
			return (T)unsafe.allocateInstance(type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
