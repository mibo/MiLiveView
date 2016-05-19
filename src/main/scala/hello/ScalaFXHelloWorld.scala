/*
 * Copyright (c) 2011-2015, ScalaFX Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the ScalaFX Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SCALAFX PROJECT OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hello

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}
import java.util.Date

import net.sourceforge.plantuml.{FileFormat, FileFormatOption, SourceStringReader}
import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.Options

import scala.io.Source
import scala.reflect.io.Path
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import scalafx.scene.web.WebView

object ScalaFXHelloWorld extends JFXApp {

  val filePath = parameters.named.getOrElse("file", "File parameter (--file=...) is not available")

  val updatedText = new Text {
    text = new Date().toString
  }
  val webContentView = new WebView() {
    engine.loadContent(loadContent())
  }

  var lastLoadedTime:Long = -1

  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)

    title = "mi Live View"
    scene = new Scene {
      fill = Color.White
      content = new VBox {
        padding = Insets(5, 10, 5, 10)

        children = Seq(
          new HBox {
            children = Seq(
              new Text {
                text = filePath
                style = "-fx-font: normal 12pt sans-serif"
              },
              updatedText
            )
          },

          webContentView
        )
      }
    }
  }

  startTask

  def reload():Unit = {
    while(true) {
      Platform.runLater({
        if(checkForUpdate()) {
          lastLoadedTime = System.currentTimeMillis()
          updatedText.text = new Date().toString
          webContentView.engine.loadContent(loadContent())
          System.out.println("Reloaded at '" + updatedText.text + "'....")
        }
      })
      Thread.sleep(2000)
    }
  }

  def startTask = {
    val backgroundThread = new Thread {
      setDaemon(true)
      override def run = {
        reload()
      }
    }
    backgroundThread.start()
  }

  def checkForUpdate(): Boolean = {
    val file = Paths.get(filePath)
    if(Files.exists(file)) {
      val lastModified = Files.getLastModifiedTime(file)
      return lastLoadedTime < lastModified.toMillis
    }
    false
  }


  def loadContent(): String = {
    if(Files.exists(Paths.get(filePath))) {
      if(filePath.endsWith("puml")) {
        return loadPuml()
      } else if(filePath.endsWith("adoc")) {
        return loadAdoc()
      }
    }
    "<html><head/><body><h1>Fail</h1><br/>File at path '" + filePath + "' does not exists.</body></html>"
  }

  def loadPuml(): String = {
    val os = new ByteArrayOutputStream()
    val content = Source.fromFile(filePath).mkString
    val reader: SourceStringReader = new SourceStringReader(content)
    val desc: String = reader.generateImage(os, new FileFormatOption(FileFormat.SVG))
    os.toString("utf-8")
  }

  def loadAdoc(): String = {
    val asciidoctor = Factory.create()
    val options = new Options()
    options.setToFile(false)
    asciidoctor.convertFile(new File(filePath), options)
  }
}