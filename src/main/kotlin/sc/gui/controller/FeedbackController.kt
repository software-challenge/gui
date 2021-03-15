package sc.gui.controller

import sc.gui.model.AppModel
import sc.gui.model.ViewType
import tornadofx.Controller
import java.io.File
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormPart
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class FeedbackController : Controller(){
    val model = AppModel()
    
    fun changeViewTo(newView: ViewType) {
        val current = model.currentView.get()
        AppController.logger.debug("Requested View change from ${current.name} -> $newView")
        if (current == newView) {
            AppController.logger.warn("Noop view change request!")
            return
        }
        find(current.view).replaceWith(newView.view)
        model.previousView.set(current)
        model.currentView.set(newView)
    }
    
    private fun formatFeedbackType(fbt: String):String{
        if (fbt == "Organisatorisches")
            return "ORGANIZATIONAL"
        if (fbt == "Dokumentation")
            return "DOCUMENTATION"
        if (fbt == "Programmieren")
            return "PROGRAMMING"
        if (fbt == "Softwarefehler")
            return "SOFTWARE"
        if (fbt == "Fehler in der Spielelogik")
            return "GAMELOGIC"
        if (fbt == "Sonstiges")
            return "MISCELLAINIOUS"
        return ""
    }
    
    fun sendFeedback(fbt: String, teamname: String, email: String, desc: String, f: File){
        val data = mutableMapOf("feedbacktype" to formatFeedbackType(fbt), "teamname" to teamname, "email" to email, "description" to desc)
        val files = mutableMapOf<String, File>()
        
        var file = f
        
        if (f.path == "")
            file = File.createTempFile("tmp","")
        
        files.put("", file)
        
        GlobalScope.async { uploadData("http://localhost/feedback", files, data) }
    }
    
    suspend fun uploadData(
            uploadUrl: String,
            uploadFiles: Map<String, File>,
            texts: Map<String, String>
    ) {
        val response: HttpResponse = HttpClient(Apache).post<HttpResponse>(uploadUrl){
            headers{
                append("Accept", "*/*")
                append("Host", "localhost")
                append("Accept-Encoding", "gzip, deflate")
            }
            body = MultiPartFormDataContent(
                formData {
                    uploadFiles.entries.forEach {
                        this.appendInput(
                                key = it.key,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentDisposition,
                                            "filename=${it.value.name}")
                                },
                                size = it.value.length()
                        ) { buildPacket { writeFully(it.value.readBytes()) } }
                    }
                    texts.entries.forEach {
                        this.append(FormPart(it.key, it.value))
                    }
                }
            )
        }
        println(response.status)
    }
}