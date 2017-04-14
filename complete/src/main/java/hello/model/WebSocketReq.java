package hello.model;

public class WebSocketReq {
	private String name;

	public WebSocketReq() {
	}
	public WebSocketReq(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "HelloMessage [name=" + name + "]";
	}
}
