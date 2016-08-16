package controllers

import java.io.File


object Utils {
  def getListOfFiles(dirName: String):List[File] = {
    val dir = new File(dirName)
    if (dir.exists && dir.isDirectory) {dir.listFiles.filter(_.isFile).filter(!_.isHidden).toList}
    else {List[File]()}
  }
}
