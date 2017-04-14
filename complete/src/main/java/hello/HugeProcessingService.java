package hello;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import hello.model.HugeProcessStatusEvent;

@Service
public class HugeProcessingService {
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public void process1(HugeProcessStatusEvent hugeProcessStatusEvent)
			throws InterruptedException, IllegalArgumentException {
		induceException1();
		Thread.sleep(500);
		reportStatusToClient(hugeProcessStatusEvent, "Task 1 @ Step 1:" + System.currentTimeMillis());
		induceException1();
		Thread.sleep(5000);
		reportStatusToClient(hugeProcessStatusEvent, "Task 1 @ Step 2:" + System.currentTimeMillis());
		induceException1();
		Thread.sleep(5000);
		reportStatusToClient(hugeProcessStatusEvent, "Task 1 @ Step 3:" + System.currentTimeMillis());
		induceException1();
		Thread.sleep(5000);
		reportStatusToClient(hugeProcessStatusEvent, "Task 1 @ Step 4:" + System.currentTimeMillis());
		induceException1();
		Thread.sleep(6000);
		hugeProcessStatusEvent.setCompleted(true);
		reportStatusToClient(hugeProcessStatusEvent, "Task 1 Compeleted:" + System.currentTimeMillis());
	}
	public void process2(HugeProcessStatusEvent hugeProcessStatusEvent)
			throws InterruptedException, IllegalArgumentException {
		for (int i = 0; i < 1000; i++) {
			Thread.sleep(4);
			reportStatusToClient(hugeProcessStatusEvent, "Task 2 @ Step " + i + ":" + System.currentTimeMillis());
		}
		hugeProcessStatusEvent.setCompleted(true);
		reportStatusToClient(hugeProcessStatusEvent, "Task 2 @ Compeleted:" + System.currentTimeMillis());
	}
	private void induceException1() throws IllegalArgumentException {
		if (new Random().nextInt(100) % 7 == 0) {
			throw new IllegalArgumentException("You are the most unlucky!:" + System.currentTimeMillis());
		}
	}
	private void reportStatusToClient(HugeProcessStatusEvent hugeProcessStatusEvent, String status) {
		hugeProcessStatusEvent.setStatus(status);
		eventPublisher.publishEvent(hugeProcessStatusEvent);
	}
}