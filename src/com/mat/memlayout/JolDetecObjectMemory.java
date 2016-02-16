package com.mat.memlayout;

import static java.lang.System.out;

import java.util.concurrent.Executors;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.util.VMSupport;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.mat.memlayout.UnsafeDetecObjectMemory.Value;
/**
 * <p>这不是金科玉律，不同的虚拟机有不同的规则，具体规则还是要根据jvm实现和java一些工具去测量</p>
 *  java 内存对齐规则：http://blog.csdn.net/lianchao668/article/details/8013138，第五个规则不符合这里。
 * @author "wangbin01"
 *
 */
public class JolDetecObjectMemory {


    /*
     * This sample showcases the basic field layout.
     * You can see a few notable things here:
     *   a) how much the object header consumes;
     *   b) how fields are laid out;
     *   c) how the external alignment beefs up the object size
     */

    public static void main(String[] args) throws Exception {
        out.println(VMSupport.vmDetails());
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
//        out.println(ClassLayout.parseClass(ringBuffer.getClass()).toPrintable());
        out.println(ClassLayout.parseClass(B.class).toPrintable());
    }

    static class A{  
        byte a;  
    }  
      
    static class B extends A{  
        long b;  
        short c;  
        byte d;  
    } 

}
