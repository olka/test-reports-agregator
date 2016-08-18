package controllers

import java.io.File

import controllers.Utils._

import scala.xml._

object XmlTestResultParser {
//  def parse(el: Elem) = TestEntry(Some((el \\ "testsuite").text).get,"",false)

  def metadata(el: Elem) = getAttributes(el,List("name","time","tests","errors","skipped","failures"))
  def getAttributes(el: Elem, atts: List[String]) = atts.map(a => a + ": " +el.attribute(a).get.text) mkString ", "

  def getJunitTestsMetadataAsString():String = {//Junit metadata
      getJunitTestsMetadata() mkString "\n"
  }

  def getJunitTestsMetadata():List[String] = {
    val files: List[File] = getListOfFiles("./dataStore");
    files.map(f => s"[${f.getName}]: "+metadata(XML.loadFile(f)))
  }
}
