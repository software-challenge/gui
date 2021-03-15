package sc.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import sc.gui.controller.AppController
import sc.gui.controller.FeedbackController
import tornadofx.*
import java.io.File

class FeedbackView : View() {
    private val controller: FeedbackController by inject()
    private val appController: AppController by inject()
    
    private val feedbackTypes = FXCollections.observableArrayList("Organisatorisches",
            "Dokumentation","Programmieren", "Softwarefehler", "Fehler in der Spielelogik", "Sonstiges")
    
    private var fbt = SimpleStringProperty()
    private var teamName: TextField = TextField("")
    private var email: TextField = TextField("")
    private var description: TextArea by singleAssign()
    private var f: File = File("")
    private var fileText = TextField("Keine Datei ausgewählt")
    
    override val root = vbox {
        alignment = Pos.TOP_LEFT
        
        vbox {
            alignment = Pos.TOP_LEFT
            padding = Insets(20.0, 200.0, 0.0, 200.0)
            
            vbox{
                label {
                    style {
                        fontSize = 23.px
                    }
                    text = "Feedbackthema:"
                }
                
                 combobox<String>(fbt, feedbackTypes)
            }
            vbox {
                label {
                    style {
                        fontSize = 23.px
                    }
                    text = "Teamname (optional):"
                }
                teamName = textfield {
                }
            }
            vbox {
                label {
                    style {
                        fontSize = 23.px
                    }
                    text = "Email (optional):"
                }
                email = textfield {
                }
            }
            vbox {
                label {
                    style {
                        fontSize = 23.px
                    }
                    text = "Beschreiben sie ihr Anliegen:"
                }
                description = textarea {
                    prefHeight = 180.0
                }
            }
            vbox {
                label {
                    style {
                        fontSize = 23.px
                    }
                    text = "Hängen sie einen Screenshot an:"
                }
                hbox {
                    button {
                        text = "Datei..."
                        setOnMouseClicked {
                            val ef = FileChooser.ExtensionFilter("Pictures", "*.png", "*.jpg", "*.jpeg")
                            val files = chooseFile("Single + non/block", arrayOf(ef))
            
                            if (files.isNotEmpty()) {
                                f = files.first()
                                fileText.text = f.name
                            }
                        }
                    }
                    fileText = textfield {
                    }
                }
            }
            hbox {
                button("Absenden") {
                    action {
                        if (!fbt.isNotNull().get())
                            alert(Alert.AlertType.ERROR, "Error", "Bitte wählen sie ein Feedbackthema")
                        else if (description.text.isEmpty())
                            alert(Alert.AlertType.ERROR, "Error", "Bitte beschreiben sie ihr Anliegen")
                        else
                            controller.sendFeedback(fbt.get(), teamName.text, email.text, description.text, f)
                    }
                }
                button("Zurück") {
                    action {
                        appController.changeViewTo(controller.model.previousView.get())
                    }
                }
            }
        }
    }
}