package patrick;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ObjectSaver {
	
	//Test Purpose
	public void setCaller(Object obj) {
		saver.caller = obj;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
		
	}
	
	private Saver saver = new Saver();
	private Loader loader = new Loader();
	
	public String toJSON(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return saver.toJSON(obj);
	}
	
	public Object fromJSON(String json) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return loader.fromJSON(json);
	}
	
	public <T> T fromJSON(String json, Class<T> type) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return loader.fromJSON(json, type);
	}
}
