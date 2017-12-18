package patrick;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

public class DUTObjectSaver extends SuperDuper {
	List<SuperDuper> listTest = new LinkedList<>();
	Field[] fields = DUTObjectSaver.class.getDeclaredFields();
	InvocationHandler handler = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(this, args);
		}
	};
	
	DUTProxy proxy = new DUTProxy() {
		@Override
		public double getDouble() {
			return 123.456;
		}
	};

	Object proxyObj = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { DUTProxy.class }, handler);
	DUTObjectSaver pointer = this;
	String test = "What would this do";
	Integer in = new Integer(1);
	Integer in2 = new Integer(1);
	int prim = 324;
	
	
	public int getInt() {
		return 222;
	}
}

class SuperDuper {
	Integer in = 321;
	SuperDuper() {
		cr = new CyclicReference(this);
	}
	CyclicReference cr;
}

class CyclicReference {
	CyclicReference(Object obj) {
		this.obj = obj;
	}
	Object obj;
}