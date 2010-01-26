/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2010, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.integration.test;


import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import schemacrawler.tools.commandline.SchemaCrawlerCommandLine;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.integration.freemarker.FreeMarkerRenderer;
import schemacrawler.tools.integration.scripting.ScriptRenderer;
import schemacrawler.tools.integration.velocity.VelocityRenderer;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.utility.TestDatabase;
import sf.util.TestUtility;

public class IntegrationTest
{

  private static TestDatabase testUtility = new TestDatabase();

  @AfterClass
  public static void afterAllTests()
  {
    testUtility.shutdownDatabase();
  }

  @BeforeClass
  public static void beforeAllTests()
    throws Exception
  {
    TestDatabase.initializeApplicationLogging();
    testUtility.createMemoryDatabase();
  }

  @Test
  public void commandlineFreeMarker()
    throws Exception
  {
    executeCommandlineAndCheckForOutputFile("freemarker",
                                            "plaintextschema.ftl",
                                            "executableForFreeMarker");
  }

  @Test
  public void commandlineJavaScript()
    throws Exception
  {
    executeCommandlineAndCheckForOutputFile("script",
                                            "plaintextschema.js",
                                            "executableForJavaScript");
  }

  @Test
  public void commandlineVelocity()
    throws Exception
  {
    executeCommandlineAndCheckForOutputFile("velocity",
                                            "plaintextschema.vm",
                                            "executableForVelocity");
  }

  @Test
  public void executableFreeMarker()
    throws Exception
  {
    executeExecutableAndCheckForOutputFile(new FreeMarkerRenderer(),
                                           "plaintextschema.ftl",
                                           "executableForFreeMarker");
  }

  @Test
  public void executableJavaScript()
    throws Exception
  {
    executeExecutableAndCheckForOutputFile(new ScriptRenderer(),
                                           "plaintextschema.js",
                                           "executableForJavaScript");
  }

  @Test
  public void executableVelocity()
    throws Exception
  {
    executeExecutableAndCheckForOutputFile(new VelocityRenderer(),
                                           "plaintextschema.vm",
                                           "executableForVelocity");
  }

  private void executeCommandlineAndCheckForOutputFile(final String command,
                                                       final String outputFormatValue,
                                                       final String referenceFileName)
    throws Exception
  {
    final File testOutputFile = File.createTempFile("schemacrawler." + command
                                                    + ".", ".test");
    testOutputFile.delete();

    final SchemaCrawlerCommandLine commandLine = new SchemaCrawlerCommandLine(testUtility
                                                                                .getDatabaseConnectionOptions(),
                                                                              "-command="
                                                                                  + command,
                                                                              "-sortcolumns=true",
                                                                              "-outputformat="
                                                                                  + outputFormatValue,
                                                                              "-outputfile="
                                                                                  + testOutputFile
                                                                                    .getAbsolutePath());
    commandLine.execute();

    final List<String> failures = new ArrayList<String>();
    TestUtility.compareOutput(referenceFileName + ".txt",
                              testOutputFile,
                              failures);
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  private void executeExecutableAndCheckForOutputFile(final Executable executable,
                                                      final String outputFormatValue,
                                                      final String referenceFileName)
    throws Exception
  {
    final File testOutputFile = File.createTempFile("schemacrawler."
                                                    + executable.getCommand()
                                                    + ".", ".test");
    testOutputFile.delete();
    final OutputOptions outputOptions = new OutputOptions(outputFormatValue,
                                                          testOutputFile
                                                            .getAbsolutePath());

    executable.getSchemaCrawlerOptions()
      .setAlphabeticalSortForTableColumns(true);
    executable.setOutputOptions(outputOptions);
    executable.execute(testUtility.getConnection());

    final List<String> failures = new ArrayList<String>();
    TestUtility.compareOutput(referenceFileName + ".txt",
                              testOutputFile,
                              failures);
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

}