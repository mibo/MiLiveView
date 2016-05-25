package de.mirb.milivi

import java.io.{ByteArrayOutputStream, File, FileReader, IOException}
import java.nio.CharBuffer
import java.nio.file.{Files, Paths}
import java.util.Date
import javafx.event.{ActionEvent, EventHandler}
import javafx.stage.FileChooser

import net.sourceforge.plantuml.{FileFormat, FileFormatOption, SourceStringReader}
import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.Options
import org.pegdown.{Extensions, LinkRenderer, PegDownProcessor, ToHtmlSerializer}

import scala.io.Source
import scala.reflect.io.Path
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ToggleButton}
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import scalafx.scene.web.WebView

object MiLiveViewApp extends JFXApp {

  var filePath = parameters.named.getOrElse("file", "File parameter (--file=...) is not available")

  val textFilePath = new Text {
    text = filePath
    style = "-fx-font: normal 12pt sans-serif"
  }
  val updatedText = new Text {
    text = new Date().toString
    alignmentInParent = Pos.CenterRight
  }
  val buttonLoadFile = new Button {
    text = "Open"
    onAction = new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = {
        loadFile()
      }
    }
  }

  val buttonToggleOnTop = new ToggleButton {
    text = "OnTop"
    onAction = new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = {
        stage.alwaysOnTop = !stage.alwaysOnTop.value
      }
    }
  }

  val webContentView = new WebView() {
    engine.loadContent(loadContent())
    maxHeight = Double.MaxValue
    maxWidth = Double.MaxValue
  }
  var lastLoadedTime:Long = -1

  stage = new PrimaryStage {
    title = "mi Live View"
    scene = new Scene {
      fill = Color.White

      root = new VBox {
        padding = Insets(5, 10, 5, 10)

        children = Seq(
          new HBox {
            padding = Insets(5, 10, 5, 10)

            children = Seq(
              textFilePath,
              buttonLoadFile,
              buttonToggleOnTop,
              updatedText
            )
          },
          webContentView
        )
      }
    }
  }

  startTask()

  //

  def loadFile(): Unit = {
    val fileChooser: FileChooser = new FileChooser
    val homeDir: File = new File(System.getProperty("user.home"))
    fileChooser.setInitialDirectory(homeDir)

    val file: File = fileChooser.showOpenDialog(stage)
    if (file != null) {
      filePath = file.getAbsolutePath
      textFilePath.text = filePath
      lastLoadedTime = -1
      reload()
    }
  }

  def reloadLoop():Unit = {
    while(true) {
      Platform.runLater({
        reload()
      })
      Thread.sleep(2000)
    }
  }

  def reload():Unit = {
    if(checkForUpdate()) {
      lastLoadedTime = System.currentTimeMillis()
      updatedText.text = new Date().toString
      webContentView.engine.loadContent(loadContent())
      System.out.println("Reloaded at '" + updatedText.text + "'....")
    }
  }

  def startTask() = {
    val backgroundThread = new Thread {
      setDaemon(true)
      override def run() = {
        reloadLoop()
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
      } else if(filePath.endsWith("md") || filePath.endsWith("markdown")) {
        return loadMarkdown()
      }
    }
    "<html><head/><body><h1>Fail</h1><br/>File at path '" + filePath + "' does not exists.</body></html>"
  }

  def loadMarkdown(): String = {
    val content = Source.fromFile(filePath).mkString
    val rootNode = new PegDownProcessor(
      Extensions.AUTOLINKS | Extensions.WIKILINKS | Extensions.FENCED_CODE_BLOCKS | Extensions.TABLES | Extensions.HARDWRAPS
    ).parseMarkdown(content.toCharArray)
    val serializer = new ToHtmlSerializer(new LinkRenderer())
    serializer.toHtml(rootNode)
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