package patrick;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.instantiator.sun.SunReflectionFactoryInstantiator;

public class ObjectSaverTestCase {

	ObjectSaver os;
	
	@Before
	public void setUp() throws Exception {
		os = new ObjectSaver();
		// Test Purpose
		os.setCaller(this);
	}
	
	@Test
	public void testWithObjenesis() {
		try {
			Objenesis objenesis = new ObjenesisStd();
			ObjectInstantiator<String> inst = objenesis.getInstantiatorOf(String.class);
			String string = inst.newInstance();
			
			if (inst instanceof SunReflectionFactoryInstantiator) {
				Field field = SunReflectionFactoryInstantiator.class.getDeclaredField("mungedConstructor");
				field.setAccessible(true);
				Constructor obj = (Constructor)field.get(inst);
				
				field = Constructor.class.getDeclaredField("constructorAccessor");
				field.setAccessible(true);
				Object obj2 = field.get(obj); // sun.reflect.ConstructorAccessor
				
				Class<?> type = obj2.getClass();
				System.out.println(type);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeObject(type);
				out.close();
				
				byte[] intermediate = baos.toByteArray();
				
				ByteArrayInputStream bais = new ByteArrayInputStream(intermediate);
				ObjectInputStream in = new ObjectInputStream(bais) {
					@Override
					protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
						String name = desc.getName();
						try {
							ClassLoader cl = type.getClassLoader();
							Field field = ClassLoader.class.getDeclaredField("classes");
							field.setAccessible(true);
							Vector<Class<?>> classes = (Vector<Class<?>>)field.get(cl);
							for (Class<?> type : classes) {
								if (type.getName().equals(name)) {
									return type;
								}
							}
							return super.resolveClass(desc);
						} catch(Exception ex) {
							return super.resolveClass(desc);
						}
					}
				};
				Object copiedType = in.readObject();
				
				
				if (type.equals(copiedType)) {
					System.out.println("Equal");
				}
				
				System.out.println(copiedType);
				
				
			}
			
			Objenesis cachedObjenesis = runCycle(objenesis, "objenesis.ser");
			String otherString = cachedObjenesis.newInstance(String.class);
			
			assertEquals(string, otherString);
		}
		catch (Exception e) {
			System.out.println("TestWithObjenesis failed");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T runCycle( T obj, String fileName ) throws IllegalArgumentException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchFieldException, SecurityException {
		String json = os.toJSON(obj);
		Path jsonDir = Paths.get("./ser");
		Files.createDirectories(jsonDir);
		Path jsonFile = jsonDir.resolve(fileName);
		try (BufferedWriter writer = Files.newBufferedWriter(jsonFile)) {
			writer.write(json);
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
		Path newJsonFile = Paths.get("./ser/" + fileName);
		String newJson = "";
		try (InputStream in = Files.newInputStream(newJsonFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			newJson = reader.lines().collect(Collectors.joining());

		} catch (IOException x) {
			System.err.println(x);
		}

		return (T) os.fromJSON(newJson);
	}

	@Test
	public void testWithMap() {
		try {
			boolean first = false;
			Map<Map, List> container = new HashMap<>();

			Map<String, Integer> key = new HashMap<>();
			key.put("WHERO", 1);
			key.put("Whatever", 3453454);
			List value = new ArrayList<>();

			container.put(key, value);

			Map cachedKey = runCycle(key, "cachedKey.ser");

			List cachedValue = container.get(cachedKey);
			
			assertTrue(value == cachedValue);

		} catch (Exception e) {
			System.out.println("TestWithMap failed");
			e.printStackTrace();
		}
	}
}
