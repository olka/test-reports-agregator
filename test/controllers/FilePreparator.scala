package controllers

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util

trait FilePreparator {
  def prepareFiles() {
    var res1 = util.Arrays.asList("<testresult name=\"Test 1\" time=\"0\" tests=\"4\" errors = \"0\" skipped=\"0\" failures=\"0\"></testresult>")
    var res2 = util.Arrays.asList("<testresult name=\"Test 2\" time=\"0\" tests=\"4\" errors = \"0\" skipped=\"0\" failures=\"99\"></testresult>")
    Files.createDirectories(Paths.get(TestEnv.TMP_FOLDER));
    Files.write(Paths.get(TestEnv.TMP_FOLDER + TestEnv.TMP_FILE), res1, Charset.forName("UTF-8"))
    Files.write(Paths.get(TestEnv.TMP_FOLDER + TestEnv.TMP_FILE + "2.xml"), res2, Charset.forName("UTF-8"))
  }

  def deleteFiles():Boolean = {
    var dir = new File(TestEnv.TMP_FOLDER)
    dir.listFiles.foreach(_.delete())
    dir.delete()
  }
}
