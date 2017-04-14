package hello;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import hello.model.WebSocketReq;
import hello.model.WebSocketRsp;

@Controller
public class GreetingController implements ApplicationListener<SessionDisconnectEvent> {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private WebSocketMessagingService webSocketMessagingService;
	private Set<String> users = new HashSet<>();

	@GetMapping("/subscribe4PrivateMsgs")
	public @ResponseBody String enablePrivateMessages(HttpSession session) {
		String sessionId = session.getId();
		users.add(sessionId);
		return sessionId;
	}
	@GetMapping("/logout")
	public @ResponseBody String logout(HttpSession session) {
		String sessionId = session.getId();
		session.invalidate();
		return sessionId;
	}
	@MessageMapping("/public")
	@SendTo("/topic/public")
	public WebSocketRsp greeting(WebSocketReq message) {
		return new WebSocketRsp("Public Hello, " + message.getName() + "!");
	}
	@MessageMapping("/private")
	@SendToUser(destinations = "/queue/private", broadcast = false)
	public WebSocketRsp privateMsg(WebSocketReq message) throws Exception {
		return new WebSocketRsp("Private Hello, " + message.getName() + "!");
	}
	@Scheduled(fixedDelay = 5000)
	private void sendPrivateMessageToScubscribers() {
		users.forEach((sessionId) -> {
			String msg = sessionId + ":" + GregorianCalendar.getInstance().getTimeInMillis();
			WebSocketRsp response = new WebSocketRsp(msg);
			messagingTemplate.convertAndSendToUser(sessionId, "/queue/private", response);
		});
	}
	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors")
	public String handleException(Throwable exception) {
		return exception.getMessage();
	}
	/**
	 * Listen on websocket-disconnects, to remove it from {@link hello.GreetingController.users}
	 */
	@Override
	public void onApplicationEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		if (sessionDisconnectEvent.getUser() != null && sessionDisconnectEvent.getUser().getName() != null) {
			users.remove(sessionDisconnectEvent.getUser().getName());
		}
	}
}
