package hello;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GreetingController {
	@Autowired
	public SimpMessagingTemplate messagingTemplate;
	private Set<String> users = new HashSet<>();

	@GetMapping("/subscribe4PrivateMsgs")
	public @ResponseBody String enablePrivateMessages(HttpSession session) {
		String sessionId = session.getId();
		users.add(sessionId);
		return sessionId;
	}
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
		Thread.sleep(1000); // simulated delay
		return new Greeting("Public Hello, " + message.getName() + "!");
	}
	@MessageMapping("/private")
	@SendToUser(destinations = "/queue/private", broadcast = false)
	public Greeting privateMsg(HelloMessage message) throws Exception {
		return new Greeting("Private Hello, " + message.getName() + "!");
	}
	@Scheduled(fixedDelay = 5000)
	private void sendPrivateMessageToScubscribers() {
		users.forEach((sessionId) -> {
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
			headerAccessor.setSessionId(sessionId);
			headerAccessor.setLeaveMutable(true);
			String msg = sessionId + ":" + GregorianCalendar.getInstance().getTimeInMillis();
			Greeting response = new Greeting(msg);
			messagingTemplate.convertAndSendToUser(sessionId, "/queue/private", response,
					headerAccessor.getMessageHeaders());
		});
	}
}
