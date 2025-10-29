package org.testconc.locks;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

import static org.junit.Assert.*;

public class StampedLockTest {

    @Test
    public void test_ReadLockUnlock() {
        StampedLock sl = new StampedLock();

        long stamp = sl.readLock();
        try {

        } finally {
            sl.unlock(stamp);
        }

        assertEquals(257, stamp);
    }

    @Test
    public void test_ReentrantLock() {
        StampedLock sl = new StampedLock();

        long stamp1 = sl.readLock();
        long stamp2 = sl.readLock();

        try {

        } finally {
            sl.unlock(stamp2);
            sl.unlock(stamp1);
        }

        assertEquals(257, stamp1);
        assertEquals(258, stamp2);
    }

    @Test
    public void test_ReentrantLock_SameOrderForUnlock() {
        StampedLock sl = new StampedLock();

        long stamp1 = sl.readLock();
        long stamp2 = sl.readLock();

        try {

        } finally {
            sl.unlock(stamp1);
            sl.unlock(stamp2);
        }
        assertEquals(257, stamp1);
        assertEquals(258, stamp2);
    }

    @Test
    public void test_ReentrantLock_WrongStamp() {
        StampedLock sl = new StampedLock();

        long stamp1 = sl.readLock();
        long stamp2 = sl.readLock();

        try {

        } finally {
            sl.unlock(259);// any number within [256;383] will work
            sl.unlock(stamp2);
        }
        assertEquals(257, stamp1);
        assertEquals(258, stamp2);
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_ReadLockUnlock_ExtraUnlock() {
        StampedLock sl = new StampedLock();

        long stamp1 = sl.readLock();

        try {

        } finally {
            sl.unlock(stamp1);
            sl.unlock(stamp1);
        }
    }

    @Test
    public void test_ReadLockValidateUnlock() {
        StampedLock sl = new StampedLock();

        long stamp1 = sl.readLock();
        long stamp2 = sl.readLock();

        try {
            assertTrue(sl.validate(stamp1));
            assertTrue(sl.validate(stamp2));// any number within [256;383] will work
        } finally {
            sl.unlock(stamp1);
            sl.unlock(stamp2);
        }

        assertEquals(257, stamp1);
        assertEquals(258, stamp2);
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_extraUnlock() {
        StampedLock sl = new StampedLock();

        long stamp = sl.writeLock();
        try {

        } finally {
            sl.unlock(stamp);
            sl.unlock(stamp);
        }
    }

    @Test
    public void test_optimisticReadLock() {
        StampedLock sl = new StampedLock();

        long optRead = sl.tryOptimisticRead();

        if (!sl.validate(optRead)) {
            optRead = sl.readLock();

            try {
            } finally {
                sl.unlock(optRead);
            }
        }

        assertEquals(256, optRead);
    }

    @Test
    public void test_ReadLockVariants() {
        StampedLock sl = new StampedLock();

        long readStamp = sl.readLock();
        long optRead = sl.tryOptimisticRead();
        long readStamp2 = sl.readLock();

        assertTrue(sl.validate(optRead));
        assertTrue(sl.validate(readStamp));
        assertTrue(sl.validate(readStamp2));

        assertEquals(256, optRead);
        assertEquals(257, readStamp);
        assertEquals(258, readStamp2);

        assertEquals(2, sl.getReadLockCount());
    }

    @Test
    public void test_asReadLock_asWriteLock() {
        StampedLock sl = new StampedLock();

        Lock readLock = sl.asReadLock();
        readLock.lock();

        assertTrue(sl.isReadLocked());
        assertFalse(sl.isWriteLocked());

        readLock.unlock();

        Lock writeLock = sl.asWriteLock();
        writeLock.lock();

        assertTrue(sl.isWriteLocked());
        assertFalse(sl.isReadLocked());
    }

    @Test
    public void test_convertReadToWrite() {
        StampedLock sl = new StampedLock();

        long readStamp = sl.readLock();
        long writeStamp = sl.tryConvertToWriteLock(readStamp);

        assertFalse(sl.validate(readStamp));
        assertTrue(sl.validate(writeStamp));
        assertFalse(sl.isReadLocked());
        assertTrue(sl.isWriteLocked());

        assertEquals(257, readStamp);
        assertEquals(384, writeStamp);
    }

    @Test
    public void test_convertWriteToRead() {
        StampedLock sl = new StampedLock();

        long writeStamp = sl.writeLock();
        long readLock = sl.tryConvertToReadLock(writeStamp);

        assertTrue(sl.validate(readLock));
        assertFalse(sl.validate(writeStamp));
    }

    @Test
    public void test_ManyReadLocks()
    {
        StampedLock sl = new StampedLock();

        for(int i=0; i<62; i++)
        {
            System.out.println(sl.readLock());
        }
        long l01 = sl.readLock();
        long l02 = sl.readLock();// last before read overflow1 == 320
        long l03 = sl.readLock();
        for(int i=0; i<60; i++)
        {
            System.out.println(sl.readLock());
        }

        long l0 = sl.readLock();// last before read overflow2 incrementing readerOverflow== 382
        long l1 = sl.readLock();// Other lock stamps will be always 382
        long l2 = sl.readLock();
        long l3 = sl.readLock();
        long l4 = sl.readLock();
        long l5 = sl.readLock();
        long l6 = sl.readLock();
        long l7 = sl.readLock();

        for(int i=0; i<Integer.MAX_VALUE; i++)
        {
            sl.readLock();
        }

        long l8 = sl.readLock();
        long l9 = sl.readLock();
        long l10 = sl.readLock(); //382
    }

}
