package hello;

import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Service
public class SessionBasedHandshakeHandler extends DefaultHandshakeHandler {
	private HttpSession session() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession(true);
	}
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		return new User(session().getId());
	}
}