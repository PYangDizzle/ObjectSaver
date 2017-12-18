package patrick;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public enum UnsafeUtil {
	INSTANCE;
	
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
		try {
			return (T)unsafe.allocateInstance(type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
