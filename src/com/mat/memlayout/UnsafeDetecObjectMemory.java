package com.mat.memlayout;

import java.util.concurrent.Executors;

import sun.misc.Unsafe;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.SingleProducerSequencer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.Util;

public class UnsafeDetecObjectMemory {
    final static Unsafe unsafe = Util.getUnsafe();

    class Value {
        String str;
    }

    class IntValue {
        Sequencer seq=new SingleProducerSequencer(2,new BlockingWaitStrategy());
//        int a = 10;
//        long b = 11;
//        int c = 12;
//        int d = 13;

    }

    public static void main(String[] args) throws Exception {
        /**
         * Ringbuffer
         */
        Disruptor<Value> dis = new Disruptor<>(new EventFactory<Value>() {

            @Override
            public Value newInstance() {
                return new UnsafeDetecObjectMemory().new Value();
            }
        }, 2, Executors.newCachedThreadPool());
        dis.start();
        dis.publishEvent(new EventTranslator<Value>() {

            @Override
            public void translateTo(Value event, long sequence) {
                event.str = "test1";
            }
        });
        dis.publishEvent(new EventTranslator<Value>() {

            @Override
            public void translateTo(Value event, long sequence) {
                event.str = "test2";
            }
        });
        RingBuffer<Value> ringBuffer = dis.getRingBuffer();
        long ringbufferAddress = addressOf(ringBuffer);
        System.out.println(ringbufferAddress);
        printBytes(ringbufferAddress, 100);
        System.out.println(unsafe.getInt(ringbufferAddress + 72));
        /**
         * Char array
         */
        char[] arrayObject = { 'x', 'y', 'z' };
        long charArrayAddress = addressOf(arrayObject);
        System.out.println("Addess: " + charArrayAddress);
        // arrayBaseOffset为16个字节
        System.out.println((char) unsafe.getByte(charArrayAddress + 16));
        /**
         * Char
         */
        char charObject = 'w';
        long charAddress = addressOf(charObject);
        System.out.println("Addess: " + charAddress);
        // http://blog.csdn.net/hao707822882/article/details/43793699,单个对象的前几个元数据占12个字节
        System.out.println((char) unsafe.getByte(charAddress + 12));
       
    }

    public static long addressOf(Object o) throws Exception {
        Object[] array = new Object[] { o };
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        int addressSize = unsafe.addressSize();
        long objectAddress;
        switch (addressSize) {
            case 4:
                objectAddress = unsafe.getInt(array, baseOffset);
                break;
            case 8:
                objectAddress = unsafe.getLong(array, baseOffset);
                break;
            default:
                throw new Error("unsupported address size: " + addressSize);
        }
        return (objectAddress);
    }

    public static void printBytes(long objectAddress, int num) {
        for (long i = 0; i < num; i++) {
            int cur = unsafe.getByte(objectAddress + i);
            System.out.print(cur + "|");
        }
        System.out.println();
    }

}
