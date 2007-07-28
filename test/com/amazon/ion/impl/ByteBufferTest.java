/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion.impl;

import java.io.IOException;
import java.util.Random;

import com.amazon.ion.IonException;
import com.amazon.ion.IonTestCase;
import com.amazon.ion.impl.IonBinary.BufferManager;

/**
 *
 */
public class ByteBufferTest
    extends IonTestCase
{
    public void testSmallInsertion()
    {
        BufferManager buf = new BufferManager();
        IonBinary.Writer writer = buf.openWriter();
        
        // Write enough data to overflow the first block
        byte[] initialData = new byte[BlockedBuffer._defaultBlockSizeMin + 5];
        writer.write(initialData, 0, initialData.length);
        
        // Now insert some stuff at the beginning
        try {
            writer.setPosition(0);
            writer.insert(10);
        }
        catch (IOException e) {
            throw new IonException(e);
        }
    }
    
    public void testLargeInsertion()
    {
        BufferManager buf = new BufferManager ();
        IonBinary.Writer writer = buf.openWriter();
        
        // Write enough data to overflow the first block
        byte[] initialData = new byte[BlockedBuffer._defaultBlockSizeMin + 5];
        writer.write(initialData, 0, initialData.length);
        
        // Now insert lots of stuff at the beginning
        try {
            writer.setPosition(0);
            writer.insert(5000);
        }
        catch (IOException e) {
            throw new IonException(e);
        }
    }
    
    public void testRandomUpdatesSmall() throws Exception
    {
        testRandomUpdates(7, 19, 100);
    }
    
    public void testRandomUpdatesMedium() throws Exception
    {
        testRandomUpdates(128, 4096, 1000);
    }
    
    public void testRandomUpdatesLarge()  throws Exception
    {
        testRandomUpdates(32*1024, 32*1024, 1000);
    }
    
    private void testRandomUpdates(int min, int max, int count) throws Exception
    {
        BlockedBuffer.setBlockSizeParameters(min, max); // make it work hard
        
        testByteBuffer testBuf = new testByteBuffer();

        BlockedBuffer blocked = new BlockedBuffer();
        BlockedBuffer.BlockedByteOutputStream blkout = new BlockedBuffer.BlockedByteOutputStream(blocked);
        BlockedBuffer.BlockedByteInputStream  blkin = new BlockedBuffer.BlockedByteInputStream(blocked); 
        
        
        final boolean debug_output = false;
        final boolean checkedOften = false;
        final boolean checkContentsEverytime = false;
        
        final int max_update = 1000;
        final int rep_count  = count;
        
        final int choiceCheck  = 0;
        final int choiceAppend = 1;
        final int choiceInsert = 2;
        final int choiceRemove = 3;
        final int choiceWrite  = 4;

        
        long seed = System.currentTimeMillis();
                
        Random r = new Random(seed);
        if (debug_output) System.out.println("seed: " + seed);
        
        byte[]  data = new byte[max_update];
        int     val = -1, len = -1, pos = 0;
        boolean justChecked = false;
        
        
        for (int ii=0; ii<rep_count; ii++) {
            int choice = r.nextInt(5);
            if (checkedOften && !justChecked) choice = choiceCheck;

            switch(choice) {
                case choiceCheck: // check
                // check doesn't need anything else
                if (justChecked) continue;
                justChecked = true;
                break;

            case choiceInsert: // insert
            case choiceRemove: // remove
            case choiceWrite:  // write
                justChecked = false;
                if (testBuf.limit() < 1) continue;
                pos = r.nextInt(testBuf.limit());
                break;
            case choiceAppend: // append
                justChecked = false;
                pos = testBuf.limit();
                break;
            default:
                assert "" == "this is a bad case in a switch statement";
                throw new RuntimeException("switch case error!");
            }

            
            switch(choice) {
            case choiceCheck: // check
                break;

            case choiceInsert: // insert
            case choiceRemove: // remove
            case choiceWrite:  // write
            case choiceAppend: // append
                len = r.nextInt(max_update);
                if (choice != choiceRemove) {
                    // every choice except remove needs a prep'd buffer
                    val = r.nextInt(32);
                    val |= (choice << 5);
                    for (int jj=0; jj<len; jj++) {
                        data[jj] = (byte)(0xff & val);
                    }
                }
                // position the buffers for the operation
                testBuf.position(pos);
                // old: buf.positionForWrite(pos);
                blkout.setPosition(pos);
                break;
            }

            switch(choice) {
            case choiceCheck: // check
                if (debug_output) System.out.println("check "+testBuf.limit());
                // old: assert testBuf.limit() == buf.size();
                if (!checkContentsEverytime && r.nextInt(1000) < 990) break; // don't do this too often
                if (testBuf.limit() > 0) {
                    testBuf.position(0);
                    // old: buf.positionForRead(0);
                    blkin.sync();
                    blkin.setPosition(0);
                    for (int jj = 0; jj<testBuf.limit(); jj++) {
                        int bt = testBuf.read();
                        // buf: int br = buf.read();
                        int bk = blkin.read();
                        if (jj > 32688 && jj < 32690) {
                            // old: assert (byte)(bt & 0xff) == (byte)(br & 0xff);
                            assert (byte)(bt & 0xff) == (byte)(bk & 0xff);
                        }
                        else {
                            // old: assert (byte)(bt & 0xff) == (byte)(br & 0xff);
                            assert (byte)(bt & 0xff) == (byte)(bk & 0xff);
                        }
                    }
                    // old: buf._validate();
                    blkin._validate();
                }
                break;
            case choiceAppend: // append
                if (debug_output) System.out.println("append "+len+" of "+val+" at "+pos);
                testBuf.write(data, len);
                // old: buf.write(data, 0, len);
                blkout.write(data, 0, len);
                break;
            case choiceInsert: // insert
                if (debug_output) System.out.println("insert "+len+" of "+val+" at "+pos);
                testBuf.insert(data, len);
                // old: buf.insert(len);
                // old: buf.write(data, 0, len);
                blkout.insert(data, 0, len);
                break;
            case choiceRemove: // remove
                if (pos + len > testBuf.limit()) {
                    if (r.nextInt(100) < 90) break;
                    len = testBuf.limit() - pos;
                }
                if (debug_output) System.out.println("remove "+len+" at "+pos);
                testBuf.remove(len);
                // old: buf.remove(len);
                blkout.remove(len);
                break;
            case choiceWrite: // write
                if (pos + len > testBuf.limit()) {
                    if (r.nextInt(100) < 90) break;
                    len = testBuf.limit() - pos;
                }
                if (debug_output) System.out.println("write "+len+" of "+val+" at "+pos);
                testBuf.write(data, len);
                // old: buf.write(data, 0, len);
                blkout.write(data, 0, len);
                break;
            default:
                assert "" == "this is a bad case in a switch statement";
                throw new RuntimeException("switch case error!");
            }
        }
    }
    
    static public class testByteBuffer
    {
        static final int startingBufferSize = 16;
        int    _position;
        int    _inUse;
        byte[] _buf;
        
        public testByteBuffer() {
            this._buf = new byte[startingBufferSize];
        }
        
        void expand(int newlen) {
            int len = this._buf.length;
            while (len < newlen) {
                len *= 2;
            }
            if (len > this._buf.length) {
                byte[] newbuf = new byte[len];
                System.arraycopy(this._buf, 0, newbuf, 0, this._inUse);
                this._buf = newbuf;
            }
        }
        public int limit() { return this._inUse; }
        public int position(int pos) {
            assert (pos >= 0 && pos <= _inUse);
            this._position = pos;
            return this._position;
        }
        public int insert(byte[] data, int len) {
            position(this._position);
            int newEnd = this._inUse + len;
            expand(newEnd);
            System.arraycopy(this._buf, this._position
                            ,this._buf, this._position + len, this._inUse - this._position);
            System.arraycopy(data, 0, this._buf, this._position, len);
            this._position += len;
            this._inUse += len;
            return len;
        }
        public int remove(int len) {
            position(this._position);
            assert (this._position + len <= this._inUse);
            System.arraycopy(this._buf, this._position + len
                            ,this._buf, this._position, this._inUse - (this._position + len));                             
            this._inUse -= len;
            return len;            
        }
        public int read() {
            position(this._position);
            assert(this._position + 1 <= this._inUse);
            int ret = this._buf[this._position];
            this._position++;
            return ret;
        }
        public byte[] read(int len) {
            position(this._position);
            assert(this._position + len <= this._inUse);
            byte[] ret = new byte[len];
            System.arraycopy(this._buf, this._position, ret, 0, len);
            this._position += len;
            return ret;
        }
        public int write(byte[] data, int len) {
            assert(data.length >= len);
            position(this._position);
            int endWrite = this._position + len; 
            expand(endWrite);
            System.arraycopy(data, 0, this._buf, this._position, len);
            this._position = endWrite;
            if (this._position > this._inUse) this._inUse = this._position;
            return len;
        }
    }
}