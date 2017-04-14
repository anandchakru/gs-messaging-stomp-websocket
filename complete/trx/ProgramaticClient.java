package hello;

import java.lang.reflect.Type;
import javax.annotation.PostConstruct;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Service
public class ProgramaticClient {
	@PostConstruct
	public void subscribe() {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new StringMessageConverter());
		StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
			@Override
			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
				super.afterConnected(session, connectedHeaders);
				session.subscribe("/topic/public", new StompFrameHandler() {
					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						System.out.println("Pub: " + payload);
					}
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return String.class;
					}
				});
				session.subscribe("/user/queue/private", new StompFrameHandler() {
					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						System.out.println("Pri: " + payload);
					}
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return String.class;
					}
				});
			}
		};
		stompClient.connect("ws://localhost:8080/websocketapi", sessionHandler);
	}
}
