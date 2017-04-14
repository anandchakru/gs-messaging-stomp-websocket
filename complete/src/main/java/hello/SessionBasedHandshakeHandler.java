package hello;

import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import hello.model.WebSocketPrincipal;

@Service
public class SessionBasedHandshakeHandler extends DefaultHandshakeHandler {
	private HttpServletRequest request() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	private HttpSession httpSession() {
		return request().getSession(true);
	}
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		String user = httpSession().getId();
		System.out.println("Alloted username:" + user);
		return new WebSocketPrincipal(user);
	}
}