package patrick;

public enum Factory {
	INSTANCE;
	
	public <T> T get(Class<T> type) {
		if (type.isAssignableFrom(JSON.class)) {
			return type.cast(new JSONJSONPImpl());
		}
		else if (type.isAssignableFrom(JSONProcessor.class)) {
			return type.cast(new JSONProcessorJSONPImpl());
		}
		else if (type.isAssignableFrom(JSONObject.class)) {
			return type.cast(new JSONObjectJSONPImpl());
		}
		else if (type.isAssignableFrom(JSONArray.class)) {
			return type.cast(new JSONArrayJSONPImpl());
		}
		else {
			return null;
		}
	}
}
