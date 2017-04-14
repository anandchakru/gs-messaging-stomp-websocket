package hello;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketMessagingService {
	/**
	 * key: httpSessionId
	 * val: WebSocketSession
	 * 
	 */
	private final Map<String, WebSocketSession> httpSessionMap = new ConcurrentHashMap<>();
	/**
	 * key: webSocketSessionId
	 * val: WebSocketSession
	 * 
	 */
	private final Map<String, WebSocketSession> webSocketSessionMap = new ConcurrentHashMap<>();

	public void registerWebSocketSession(WebSocketSession session) {
		httpSessionMap.put(session.getPrincipal().getName(), session);
		webSocketSessionMap.put(session.getId(), session);
	}
	/**
	 * 
	 * @param httpSessionId
	 * @return webSocketSessionId
	 * @throws IOException
	 */
	public String disconnectByHttpSessionId(String httpSessionId) {
		return disconnect(httpSessionMap.get(httpSessionId));
	}
	public String disconnectByWebSocketSessionId(String webSocketSessionId) {
		return disconnect(webSocketSessionMap.get(webSocketSessionId));
	}
	public String disconnect(WebSocketSession webSocketSession) {
		if (webSocketSession == null) {
			return null;
		}
		String id = webSocketSession.getId();
		try {
			webSocketSession.close();
			System.out.println("Disconnect Success!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Disconnect Failed !");
		}
		return id;
	}
}
