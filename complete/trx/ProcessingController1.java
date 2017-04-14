package hello;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import hello.model.HugeProcessStatusEvent;
import hello.model.WebSocketReq;
import hello.model.WebSocketRsp;

@Controller
public class ProcessingController {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private HugeProcessingService processingService;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

	@GetMapping("/subscribe4PrivateMsgs")
	public @ResponseBody String enablePrivateMessages(HttpSession session) throws InterruptedException {
		String sessionId = session.getId();
		System.out.println("Processing started for: " + sessionId);
		scheduler.submit(() -> {
			try {
				//TODO: How to avoid this ??!?
				// If we don't sleep, $.get("/subscribe4PrivateMsgs", funct..  takes a few ms to be ready.
				// By then few messages are dropped, since the ui is not ready to listen yet.
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			HugeProcessStatusEvent hugeProcessStatusEvent = new HugeProcessStatusEvent(sessionId, "Started");
			hugeProcessStatusEvent.setGenerationId(UUID.randomUUID().toString().replaceAll("-", ""));
			try {
				processingService.process2(hugeProcessStatusEvent);
				messagingTemplate.convertAndSend("/topic/public",
						new WebSocketRsp("Public completed: " + hugeProcessStatusEvent.getSessionId() + "!"));
			} catch (InterruptedException e) {
				hugeProcessStatusEvent.setStatus("Aborted with Exception: " + e.getMessage());
				handleHugeProcessStatusEvent(hugeProcessStatusEvent);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				hugeProcessStatusEvent.setStatus("Aborted with Exception: " + e.getMessage());
				handleHugeProcessStatusEvent(hugeProcessStatusEvent);
				e.printStackTrace();
			}
		});
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
	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors")
	public String handleException(Throwable exception) {
		return exception.getMessage();
	}
	@EventListener
	public void handleHugeProcessStatusEvent(HugeProcessStatusEvent hugeProcessStatusEvent) {
		WebSocketRsp response = new WebSocketRsp(
				hugeProcessStatusEvent.getSessionId() + ":" + hugeProcessStatusEvent.getStatus());
		System.out.println(hugeProcessStatusEvent.getSessionId() + ":" + hugeProcessStatusEvent.getStatus());
		messagingTemplate.convertAndSendToUser(hugeProcessStatusEvent.getSessionId(), "/queue/private", response);
	}
}
