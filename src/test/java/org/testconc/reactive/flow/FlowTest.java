package org.testconc.reactive.flow;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

public class FlowTest {

    @Test
    public void test_SubscribeAndConsume() throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);
        CountDownLatch cdl2 = new CountDownLatch(1);
        Flow.Subscription[] subscriptions = new Flow.Subscription[4];

        final Flow.Subscriber<String> subscriber = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(final Flow.Subscription subs) {
                System.out.println("On Subscribe");
                subscriptions[0] = subs;
                cdl.countDown();
            }

            @Override
            public void onNext(final String item) {
                System.out.println("On Next: item = " + item);
                cdl2.countDown();
            }

            @Override
            public void onError(final Throwable throwable) {
                System.out.println("On Error");
            }

            @Override
            public void onComplete() {
                System.out.println("On Complete");
            }
        };


        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        publisher.subscribe(subscriber);
        cdl.await();

        publisher.submit(new String("Some Item"));
        subscriptions[0].request(1);
        cdl2.await();
        publisher.offer(new String("Some Item"), 1000, TimeUnit.MILLISECONDS, (t, u) -> true);
//
//        publisher.isSubscribed(subscriber);
//
//        publisher.close();
//        publisher.consume((str) -> System.out.println("accepted string: " + str));
//        publisher.closeExceptionally(new IllegalStateException());
//        publisher.isClosed();
//
//        publisher.estimateMaximumLag();
//        publisher.estimateMinimumDemand();
//        publisher.getClosedException();
//        publisher.getExecutor();
//
//        publisher.getMaxBufferCapacity();
//        publisher.getNumberOfSubscribers();
//        publisher.getSubscribers();
//        publisher.hasSubscribers();




    }

    @Test
    public void test_throwNull()  {
        throw null;
    }
}
