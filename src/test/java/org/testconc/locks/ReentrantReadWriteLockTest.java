package org.testconc.locks;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {

    @Test
    public void test_ReadWriteLock() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            readLock.lock();
            System.out.println("Start read thread 1");
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    if (elems[i] != null) {
                        System.out.println(elems[i] + " - " + Thread.currentThread().getName());
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();
        new Thread(() -> {
            readLock.lock();
            System.out.println("Start read thread 2");
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    if (elems[i] != null) {
                        System.out.println(elems[i] + " - " + Thread.currentThread().getName());
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();

        new Thread(() -> {
            writeLock.lock();
            System.out.println("Start write thread 1");
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    elems[i] = "Value" + i;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }).start();

        Thread.sleep(11000);

        readLock.lock();
        System.out.println("Start main read thread");
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                if (elems[i] != null) {
                    System.out.println(elems[i] + " - " + Thread.currentThread().getName());
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_DoubleUnlock() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            readLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();
        new Thread(() -> {
            readLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();

        Thread.sleep(1000);
        readLock.lock();
        try {
            //SOMETHING here
        } finally {
            readLock.unlock();
            readLock.unlock();
        }
    }

    @Test
    public void test_ReadThenWrite() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            readLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();
        new Thread(() -> {
            readLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();

        Thread.sleep(1000);

        Assert.assertFalse(writeLock.tryLock());
    }

    @Test
    public void test_DoubleWrite() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            writeLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }).start();

        Thread.sleep(1000);

        Assert.assertFalse(writeLock.tryLock());
    }

    @Test
    public void test_WriteThenRead() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            writeLock.lock();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }).start();

        Thread.sleep(1000);

        Assert.assertFalse(readLock.tryLock());
    }

    @Test
    public void test_ReadThenWriteThenRead() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        new Thread(() -> {
            readLock.lock();
            try {
                System.out.println("First read lock");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }).start();
        Thread.sleep(300);
        new Thread(() -> {
            writeLock.lock();
            try {
                System.out.println("Second write lock");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }).start();

        Thread.sleep(1000);

        readLock.lock();
        System.out.println("Third read lock");
    }

    @Test
    public void test_DowngradeWriterToReader() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        writeLock.lock();

        readLock.lock();
    }

    @Test
    public void test_UpgradeReaderToWriter() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        String[] elems = new String[10];

        readLock.lock();

//        writeLock.lock();
    }
}
