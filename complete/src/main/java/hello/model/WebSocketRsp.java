package hello.model;

public class WebSocketRsp {
	private String content;

	public WebSocketRsp() {
	}
	public WebSocketRsp(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "Greeting [content=" + content + "]";
	}
}