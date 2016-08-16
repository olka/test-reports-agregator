package models

import play.api.libs.json.Json

case class TestEntry(title: String, message: String, isFailed: Boolean)

object TestEntry {
  implicit val testEntry = Json.format[TestEntry]
}