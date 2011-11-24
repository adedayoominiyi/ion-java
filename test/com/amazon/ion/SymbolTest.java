// Copyright (c) 2007-2011 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion;

import com.amazon.ion.impl.IonImplUtils;
import org.junit.Test;



public class SymbolTest
    extends TextTestCase
{

    public static void checkNullSymbol(IonSymbol value)
    {
        assertSame(IonType.SYMBOL, value.getType());
        assertTrue("isNullValue() is false",   value.isNullValue());
        assertNull("stringValue() isn't null", value.stringValue());

        try {
            value.getSymbolId();
            fail("Expected NullValueException");
        }
        catch (NullValueException e) { }
    }

    public void modifySymbol(IonSymbol value)
    {
        String modValue = "sweet!";

        // Make sure the test case isn't starting in the desired state.
        assertFalse(modValue.equals(value.stringValue()));

        value.setValue(modValue);
        assertEquals(modValue, value.stringValue());
        assertFalse(value.isNullValue());
//        assertTrue(value.intValue() > 0);

        String modValue1 = "dude!";
        value.setValue(modValue1);
        assertEquals(modValue1, value.stringValue());
        int sid = value.getSymbolId();

        try {
            value.setValue("");
            fail("Expected EmptySymbolException");
        }
        catch (EmptySymbolException e) { }
        assertEquals(modValue1, value.stringValue());
        assertEquals(sid, value.getSymbolId());

        value.setValue(null);
        checkNullSymbol(value);
    }



    @Override
    protected String wrap(String ionText)
    {
        return "'" + ionText + "'";
    }


    //=========================================================================
    // Test cases

    @Test
    public void testFactorySymbol()
    {
        IonSymbol value = system().newNullSymbol();
        checkNullSymbol(value);
        modifySymbol(value);

        value = system().newSymbol("hello");
    }

    public void testFactorySymbolWithSid(ValueFactory vf)
    {
        final int sid = 99;
        InternedSymbol is = IonImplUtils.newInternedSymbol(null, sid);

        IonSymbol value = vf.newSymbol(is);
        assertFalse(value.isNullValue());
        assertEquals(sid, value.getSymbolId());

        try {
            String s = value.stringValue();
            // TODO should throw
            // if we get an answer it's synthetic
            assertEquals("$"+sid, s);
//            fail("Expected exception");
        }
        catch (UnknownSymbolException e) {
            assertEquals(sid, e.getSid());
        }


        is = IonImplUtils.newInternedSymbol("text", sid);
        value = vf.newSymbol(is);
        assertFalse(value.isNullValue());
        assertEquals("text", value.stringValue());

        // TODO this needs to be well-defined
//        assertEquals(sid, value.getSymbolId());
    }

    @Test
    public void testFactorySymbolWithSid()
    {
        ValueFactory vf = system();
        testFactorySymbolWithSid(vf);

        IonList list = system().newEmptyList();
        vf = list.add();
        testFactorySymbolWithSid(vf);
    }

    @Test
    public void testNullSymbol()
    {
        IonSymbol value = (IonSymbol) oneValue("null.symbol");
        checkNullSymbol(value);
        assertArrayEquals(new String[0], value.getTypeAnnotations());
        modifySymbol(value);

        value = (IonSymbol) oneValue("a::null.symbol");
        checkNullSymbol(value);
        checkAnnotation("a", value);
        modifySymbol(value);
    }


    @Test
    public void testPlusInfinitySymbol()
    {
        system().singleValue("{value:special::'+infinity'}");
    }


    @Test
    public void testSymbols()
    {
        IonSymbol value = (IonSymbol) oneValue("foo");
        assertSame(IonType.SYMBOL, value.getType());
        assertEquals("foo", value.stringValue());
        assertTrue(value.getSymbolId() > 0);
        modifySymbol(value);

        value = (IonSymbol) oneValue("'foo'");
        assertSame(IonType.SYMBOL, value.getType());
        assertEquals("foo", value.stringValue());
        assertTrue(value.getSymbolId() > 0);
        modifySymbol(value);

        value = (IonSymbol) oneValue("'foo bar'");
        assertSame(IonType.SYMBOL, value.getType());
        assertEquals("foo bar", value.stringValue());
        assertTrue(value.getSymbolId() > 0);
        modifySymbol(value);
    }


    @Test
    public void testSyntheticSymbols()
    {
        String symText = "$324";
        IonSymbol value = (IonSymbol) oneValue(symText);
        checkSymbol(null, 324, value);
    }


    @Test
    public void testQuotesOnMediumStringBoundary()
    {
        // Double-quote falls on the boundary.
        checkSymbol("KIM 12\" X 12\"", oneValue("'KIM 12\\\" X 12\\\"'"));
    }


    @Test
    public void testSymbolClone()
        throws Exception
    {
        testSimpleClone("null.symbol");
        testSimpleClone("root");
    }


    @Test
    public void testClone()
    {
        IonValue data = system().singleValue("root");
        IonValue clone = data.clone();
        assertEquals(data, clone);
    }


    @Test
    public void testSymbolWithEscapedNewline()
    {
        badValue("'\\\n'");
    }
}
