package com.netflix.edda

import java.util.Properties
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC

import org.scalatest.FunSuite

class UtilsTest extends FunSuite {
    test("getProperty") {
        val props = new Properties
        props.setProperty("edda.setting", "value")
        expect("value") {
            Utils.getProperty(props, "edda", "setting", "my.context", "default")
        }
        
        props.clear()
        props.setProperty("edda.my.setting", "value")
        expect("value") {
            Utils.getProperty(props, "edda", "setting", "my.context", "default")
        }

        props.clear()
        props.setProperty("edda.my.context.setting", "value")
        expect("value") {
            Utils.getProperty(props, "edda", "setting", "my.context", "default")
        }

        props.clear()
        props.setProperty("edda.other.context.setting", "value")
        expect("default") {
            Utils.getProperty(props, "edda", "setting", "my.context", "default")
        }
    }

    test("toPrettyJson") {
        val date = new DateTime(0, UTC)
        val expected = """{
  "date" : "1969-12-31T16:00:00.0Z",
  "foo" : "bar",
  "list" : [
    1,
    2,
    3,
    4
  ]
}"""
        expect(expected) {
            Utils.toPrettyJson(Map("foo" -> "bar", "list" -> List(1,2,3,4), "date" -> date))
        }
    }

    test("toJson") {
        val date = new DateTime(0, UTC)
        val expected = """{"date":0,"foo":"bar","list":[1,2,3,4]}"""
        expect(expected) {
            Utils.toJson(Map("foo" -> "bar", "list" -> List(1,2,3,4), "date" -> date))
        }
        expect(expected) {
            Utils.toJson(Map("foo" -> "bar", "list" -> Seq(1,2,3,4), "date" -> date))
        }
        expect(expected) {
            Utils.toJson(Map("foo" -> "bar", "list" -> Range(1,5), "date" -> date))
        }
    }

    test("diffRecords") {
        val r1 = Record("id", 1).copy(stime=new DateTime(0, UTC))
        val r2 = Record("id", 2).copy(stime=new DateTime(1, UTC))
        var expected = """--- collection/path/id;_pp;_at=0
+++ collection/path/id;_pp;_at=1
@@ -1,1 +1,1 @@
-1
+2
"""
        expect(expected) {
            Utils.diffRecords(Seq(r2,r1), None, "collection/path")
        }

        val r3 = Record("id", List("this", "is", "a", "test")).copy(stime=new DateTime(0, UTC))
        val r4 = Record("id", List("this", "is", "a", "great", "test")).copy(stime=new DateTime(1, UTC))
        val r5 = Record("id", List("this", "is", "an", "even", "better", "test")).copy(stime=new DateTime(2, UTC))

        expected = """--- collection/path/id;_pp;_at=1
+++ collection/path/id;_pp;_at=2
@@ -1,7 +1,8 @@
 [
   "this",
   "is",
-  "a",
-  "great",
+  "an",
+  "even",
+  "better",
   "test"
 ]
--- collection/path/id;_pp;_at=0
+++ collection/path/id;_pp;_at=1
@@ -1,6 +1,7 @@
 [
   "this",
   "is",
   "a",
+  "great",
   "test"
 ]
"""
        expect(expected) {
            Utils.diffRecords(Seq(r5,r4,r3), None, "collection/path")
        }
        
        expected = """--- collection/path/id;_pp;_at=1
+++ collection/path/id;_pp;_at=2
@@ -4,2 +4,3 @@
-  "a",
-  "great",
+  "an",
+  "even",
+  "better",
--- collection/path/id;_pp;_at=0
+++ collection/path/id;_pp;_at=1
@@ -5,0 +5,1 @@
+  "great",
"""
        expect(expected) {
            Utils.diffRecords(Seq(r5,r4,r3), Some(0), "collection/path")
        }
    }
}
