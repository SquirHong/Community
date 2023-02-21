package com.hjs.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author hong
 * @create 2023-01-28 16:14
 */
public class blockQueueTest {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);
        new Thread(new producer(queue)).start();
        new Thread(new Comsumer(queue)).start();
        new Thread(new Comsumer(queue)).start();
        new Thread(new Comsumer(queue)).start();
    }
}

class producer implements Runnable {
    BlockingQueue<Integer> queue;

    producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            try {
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "线程生产消息,size大小为" + queue.size());
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


class Comsumer implements Runnable {
    BlockingQueue<Integer> queue;

    Comsumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "线程消费消息,size大小为" + queue.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

