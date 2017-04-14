package hello;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	@Autowired
	private AbstractSubscribableChannel clientInboundChannel;
	@Autowired
	private AbstractSubscribableChannel clientOutboundChannel;
	@Autowired
	private WebSocketMessagingService webSocketMessagingService;
	@Autowired
	private SessionBasedHandshakeHandler handshakeHandler;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(new ChannelInterceptorAdapter() {
			@Override
			public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
				System.out.println("Output Msg: " + new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
				super.postSend(message, channel, sent);
			}
		});
		super.configureClientOutboundChannel(registration);
	}
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(new ChannelInterceptorAdapter() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
				StompCommand command = accessor.getCommand();
				System.out.println("Input Msg: " + command.toString());
				return super.preSend(message, channel);
			}
		});
		super.configureClientInboundChannel(registration);
	}
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue"); //TODO: Need Rabbit/Active MQ for scalability purpose 
		config.setApplicationDestinationPrefixes("/app");
	}
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		super.configureWebSocketTransport(registration);
	}
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/websocketapi").setHandshakeHandler(handshakeHandler).withSockJS();
	}
	@Bean
	public WebSocketHandler subProtocolWebSocketHandler() {
		return new SubProtocolWebSocketHandler(clientInboundChannel, clientOutboundChannel) {
			@Override
			public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
				webSocketMessagingService.registerWebSocketSession(webSocketSession);
				scheduler.schedule(() -> {
					webSocketMessagingService.disconnect(webSocketSession);
				}, 10, TimeUnit.MINUTES);
				super.afterConnectionEstablished(webSocketSession);
			}
		};
	}
}