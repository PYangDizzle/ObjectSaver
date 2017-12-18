package patrick;

public class UniqueObjectWrapper {
	
	public Object obj;
	public int id = -1;
	
	// Factory
	public UniqueObjectWrapper() {
	}
	
	public UniqueObjectWrapper wrap(Object obj) {
		id++;
		return new UniqueObjectWrapper(obj, id);
	}
	
	// Instance
	private UniqueObjectWrapper(Object obj, int id) {
		this.obj = obj;
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this.getClass().isAssignableFrom(obj.getClass())) {
			return this.obj == this.getClass().cast(obj).obj; 
		}
		else {
			return this.obj == obj;
		}
	}
	
	@Override
	public int hashCode() {
		return id;
	}
}
