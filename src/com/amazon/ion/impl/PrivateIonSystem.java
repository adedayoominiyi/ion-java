/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.amazon.ion.impl;

import com.amazon.ion.IonContainer;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonValue;
import com.amazon.ion.IonWriter;
import com.amazon.ion.SymbolTable;
import com.amazon.ion.system.IonSystemBuilder;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

/**
 * @deprecated This is an internal API that is subject to change without notice.
 */
@Deprecated
public interface PrivateIonSystem
    extends IonSystem
{
    public SymbolTable newSharedSymbolTable(IonStruct ionRep);

    /**
     * TODO Must correct amznlabs/ion-java#63 before exposing this or using from public API.
     */
    public Iterator<IonValue> systemIterate(String ionText);

    /**
     * TODO Must correct amznlabs/ion-java#63 before exposing this or using from public API.
     */
    public Iterator<IonValue> systemIterate(Reader ionText);

    public Iterator<IonValue> systemIterate(byte[] ionData);

    /**
     * TODO Must correct amznlabs/ion-java#63 before exposing this or using from public API.
     */
    public Iterator<IonValue> systemIterate(InputStream ionData);

    public IonReader newSystemReader(Reader ionText);

    public IonReader newSystemReader(byte[] ionData);

    public IonReader newSystemReader(byte[] ionData, int offset, int len);

    public IonReader newSystemReader(String ionText);

    public IonReader newSystemReader(InputStream ionData);

    public IonReader newSystemReader(IonValue value);


    public IonWriter newTreeWriter(IonContainer container);

    public IonWriter newTreeSystemWriter(IonContainer container);


    public boolean valueIsSharedSymbolTable(IonValue value);

    /**
     * Indicates whether writers built by this system may attempt to optimize
     * {@link IonWriter#writeValue(IonReader)} by copying raw source data.
     *
     * @see IonSystemBuilder#isStreamCopyOptimized()
     */
    public boolean isStreamCopyOptimized();
}