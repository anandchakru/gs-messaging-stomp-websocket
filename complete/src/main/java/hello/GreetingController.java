package hello;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
	@Autowired
	public SimpMessageSendingOperations messagingTemplate2;
	private Set<String> users = new HashSet<>();

	@GetMapping("/subscribe4PrivateMsgs")
	public @ResponseBody String enablePrivateMessages(HttpSession session) {
		String sessionId = session.getId();
		users.add(sessionId);
		return sessionId;
	}
	@MessageMapping("/public")
	@SendTo("/topic/public")
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
			String msg = sessionId + ":" + GregorianCalendar.getInstance().getTimeInMillis();
			Greeting response = new Greeting(msg);
			messagingTemplate.convertAndSendToUser(sessionId, "/queue/private", response);
			/*
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
			headerAccessor.setSessionId(sessionId);
			headerAccessor.setLeaveMutable(true);
			messagingTemplate.convertAndSendToUser(sessionId, "/queue/private", response,
					headerAccessor.getMessageHeaders());
			*/
		});
	}
	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors")
	public String handleException(Throwable exception) {
		return exception.getMessage();
	}
}
