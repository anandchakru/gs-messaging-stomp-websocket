package hello;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GreetingController {
	@Autowired
	public SimpMessageSendingOperations messagingTemplate;
	private Set<String> sessionIds = new HashSet<>();

	@GetMapping("/subscribe4PrivateMsgs")
	public @ResponseBody String enablePrivateMessages(HttpSession session) {
		String sessionId = session.getId();
		sessionIds.add(sessionId);
		return sessionId;
	}
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
		Thread.sleep(1000); // simulated delay
		return new Greeting("Hello, " + message.getName() + "!");
	}
	@Scheduled(fixedDelay = 5000)
	private void sendPrivateMessageToScubscribers() {
		sessionIds.forEach((sessionId) -> {
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
			headerAccessor.setSessionId(sessionId);
			headerAccessor.setLeaveMutable(true);
			String msg = sessionId + ":" + GregorianCalendar.getInstance().getTimeInMillis();
			Greeting response = new Greeting(msg);
			System.out.println("Sending, " + response);
			messagingTemplate.convertAndSendToUser(sessionId, "/queue/private", response,
					headerAccessor.getMessageHeaders());
			messagingTemplate.convertAndSendToUser(sessionId, "/user/queue/private", response,
					headerAccessor.getMessageHeaders());
			messagingTemplate.convertAndSendToUser(sessionId, "/user/" + sessionId + "/queue/private", response,
					headerAccessor.getMessageHeaders());
			messagingTemplate.convertAndSend("/topic/greetings", response);
		});
	}
}
