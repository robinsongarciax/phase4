/*
 * Copyright (C) 2015-2025 Philip Helger (www.helger.com)
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
package com.helger.phase4.peppol.supplementary.tools;

import java.io.File;

import com.helger.commons.base64.Base64;
import com.helger.commons.io.file.SimpleFileIO;

public final class MainBase64
{
  public static void main (final String [] args)
  {
    // adopt path to your needs
    final File f = new File ("src/test/resources/test-ap.p12");
    final byte [] b = SimpleFileIO.getAllFileBytes (f);
    SimpleFileIO.writeFile (new File (f.getAbsolutePath () + ".b64"), Base64.safeEncodeBytesToBytes (b));
  }
}
