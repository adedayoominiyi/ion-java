/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion;



public class SexpTest
    extends SequenceTestCase
{
    @Override
    protected IonSequence makeNull()
    {
        return system().newSexp();
    }

    
    //=========================================================================
    // Test cases

    public void testFactoryNullSexp()
    {
        IonSexp value = system().newSexp();
        assertNull(value.getContainer());
        testFreshNullSequence(value);
    }

    public void testTextNullSexp()
    {
        IonSexp value = (IonSexp) oneValue("null.sexp");
        testFreshNullSequence(value);
    }
    
    public void testMakeNullSexp()
    {
        IonSexp value = (IonSexp) oneValue("(foo+bar)");
        assertFalse(value.isNullValue());
        value.makeNull();
        testFreshNullSequence(value);
    }
    
    public void testClearNonMaterializedSexp()
    {
        IonSexp value = (IonSexp) oneValue("(foo+bar)");
        testClearContainer(value);
    }

    public void testEmptySexp()
    {
        IonSexp value = (IonSexp) oneValue("()");
        testEmptySequence(value);
    }

    public void testGetTwiceReturnsSame()
    {
        IonSexp value = (IonSexp) oneValue("(a b)");
        IonValue elt1 = value.get(1);
        IonValue elt2 = value.get(1);
        assertSame(elt1, elt2);
    }


    public void testTrickyParsing()
        throws Exception
    {
        IonSexp value = (IonSexp) oneValue("(a+b::c)");
        checkSymbol("a", value.get(0));
        checkSymbol("+", value.get(1));
        IonSymbol val3 = (IonSymbol) value.get(2);
        checkAnnotation("b", val3);
        checkSymbol("c", val3);
        assertEquals(3, value.size());
    }
    
    /** Ensure that triple-quote concatenation works inside sexpr. */
    public void testConcatenation()
    {
        IonSexp value = (IonSexp) oneValue("(a '''a''' '''b''' \"c\")");
        checkSymbol("a",  value.get(0));
        checkString("ab", value.get(1));
        checkString("c",  value.get(2));
        assertEquals(3, value.size());        
    }
    
    public void testSexpIteratorRemove()
    {
        IonSexp value = (IonSexp) oneValue("(a b c)");
        testIteratorRemove(value);
    }
}