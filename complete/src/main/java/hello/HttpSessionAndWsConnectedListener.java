package hello;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Service
public class HttpSessionAndWsConnectedListener
		implements HttpSessionListener, ApplicationListener<SessionConnectedEvent> {
	private static final Map<String, HttpSession> httpSessions = new HashMap<String, HttpSession>();
	@Autowired
	private WebSocketMessagingService webSocketDisconnectService;

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		String sessionId = se.getSession().getId();
		httpSessions.remove(se.getSession().getId());
		webSocketDisconnectService.disconnectByHttpSessionId(sessionId);
	}
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		httpSessions.put(se.getSession().getId(), se.getSession());
	}
	/**
	 * If WebSocket connection established without sessionId
	 */
	@Override
	public void onApplicationEvent(SessionConnectedEvent event) {
		if (event.getUser() != null && event.getUser().getName() != null) {
			String httpSessionId = event.getUser().getName();
			HttpSession find = httpSessions.get(httpSessionId);
			if (find == null) {
				webSocketDisconnectService.disconnectByWebSocketSessionId(httpSessionId);
			}
		} else {
			System.out.println("This shouldn't happen, Intruder?");
		}
	}
}
