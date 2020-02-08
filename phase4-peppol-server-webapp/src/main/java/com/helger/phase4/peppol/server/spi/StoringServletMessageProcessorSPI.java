/**
 * Copyright (C) 2020 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.phase4.peppol.server.spi;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.phase4.attachment.WSS4JAttachment;
import com.helger.phase4.ebms3header.Ebms3Error;
import com.helger.phase4.ebms3header.Ebms3SignalMessage;
import com.helger.phase4.ebms3header.Ebms3UserMessage;
import com.helger.phase4.marshaller.Ebms3NamespaceHandler;
import com.helger.phase4.messaging.IAS4IncomingMessageMetadata;
import com.helger.phase4.model.pmode.IPMode;
import com.helger.phase4.peppol.server.storage.StorageHelper;
import com.helger.phase4.servlet.IAS4MessageState;
import com.helger.phase4.servlet.spi.AS4MessageProcessorResult;
import com.helger.phase4.servlet.spi.AS4SignalMessageProcessorResult;
import com.helger.phase4.servlet.spi.IAS4ServletMessageProcessorSPI;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;

/**
 * Example implementation of {@link IAS4ServletMessageProcessorSPI}
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class StoringServletMessageProcessorSPI implements IAS4ServletMessageProcessorSPI
{
  private static final Logger LOGGER = LoggerFactory.getLogger (StoringServletMessageProcessorSPI.class);

  private static void _dumpSoap (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                 @Nonnull final IAS4MessageState aState)
  {
    final File aFile = StorageHelper.getStorageFile (aMessageMetadata, ".soap");

    final Document aSoapDoc = aState.hasDecryptedSoapDocument () ? aState.getDecryptedSoapDocument ()
                                                                 : aState.getOriginalSoapDocument ();
    final MapBasedNamespaceContext aNSCtx = new Ebms3NamespaceHandler ();
    final byte [] aBytes = XMLWriter.getNodeAsBytes (aSoapDoc, new XMLWriterSettings ().setNamespaceContext (aNSCtx));
    if (SimpleFileIO.writeFile (aFile, aBytes).isFailure ())
      LOGGER.error ("Failed to write SOAP to '" + aFile.getAbsolutePath () + "'");
    else
      LOGGER.info ("Wrote SOAP to '" + aFile.getAbsolutePath () + "'");
  }

  @Nonnull
  public AS4MessageProcessorResult processAS4UserMessage (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                                          @Nonnull final HttpHeaderMap aHttpHeaders,
                                                          @Nonnull final Ebms3UserMessage aUserMessage,
                                                          @Nonnull final IPMode aPMode,
                                                          @Nullable final Node aPayload,
                                                          @Nullable final ICommonsList <WSS4JAttachment> aIncomingAttachments,
                                                          @Nonnull final IAS4MessageState aState,
                                                          @Nonnull final ICommonsList <Ebms3Error> aProcessingErrorMessages)
  {
    LOGGER.info ("Received AS4 user message");
    _dumpSoap (aMessageMetadata, aState);
    return AS4MessageProcessorResult.createSuccess ();
  }

  @Nonnull
  public AS4SignalMessageProcessorResult processAS4SignalMessage (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                                                  @Nonnull final HttpHeaderMap aHttpHeaders,
                                                                  @Nonnull final Ebms3SignalMessage aSignalMessage,
                                                                  @Nullable final IPMode aPMode,
                                                                  @Nonnull final IAS4MessageState aState,
                                                                  @Nonnull final ICommonsList <Ebms3Error> aProcessingErrorMessages)
  {
    if (aSignalMessage.getReceipt () != null)
    {
      // Receipt - just acknowledge
      LOGGER.info ("Received AS4 Receipt");
      _dumpSoap (aMessageMetadata, aState);
      return AS4SignalMessageProcessorResult.createSuccess ();
    }

    if (!aSignalMessage.getError ().isEmpty ())
    {
      // Error - just acknowledge
      LOGGER.info ("Received AS4 Error");
      _dumpSoap (aMessageMetadata, aState);
      return AS4SignalMessageProcessorResult.createSuccess ();
    }

    if (aSignalMessage.getPullRequest () != null)
    {
      // Must be a pull-request
      LOGGER.info ("Received AS4 Pull-Request");
      _dumpSoap (aMessageMetadata, aState);
      return AS4SignalMessageProcessorResult.createSuccess ();
    }

    LOGGER.warn ("Received an unexpected signal message - see file");
    _dumpSoap (aMessageMetadata, aState);
    return AS4SignalMessageProcessorResult.createSuccess ();
  }

  public void processAS4ResponseMessage (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                         @Nonnull final IAS4MessageState aState,
                                         @Nullable final byte [] aResponseBytes,
                                         final boolean bResponsePayloadIsAvailable)
  {
    final File aFile = StorageHelper.getStorageFile (aMessageMetadata, ".response");
    if (SimpleFileIO.writeFile (aFile, aResponseBytes).isFailure ())
      LOGGER.error ("Failed to write response to '" + aFile.getAbsolutePath () + "'");
    else
      LOGGER.info ("Wrote response to '" + aFile.getAbsolutePath () + "'");
  }
}