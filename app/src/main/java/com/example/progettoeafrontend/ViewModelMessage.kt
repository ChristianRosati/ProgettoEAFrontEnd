package com.example.progettoeafrontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoeafrontend.model.Message
import com.example.progettoeafrontend.network.Service
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface UiStateMessage {
    data class Success(val map: MutableMap<Pair<String, String>, MutableList<Message>>) : UiStateMessage
    object Error : UiStateMessage
    object Loading : UiStateMessage
}

sealed interface UiStateSendMessage {
    object Success : UiStateSendMessage
    object Error : UiStateSendMessage
    object Loading : UiStateSendMessage
}


class viewModelMessage : ViewModel(){

    var uiStateMessage:UiStateMessage by mutableStateOf(UiStateMessage.Loading)
        private set

    var uiStateSendMessage:UiStateSendMessage by mutableStateOf(UiStateSendMessage.Loading)
        private set


    init{
//        getMessages()
    }

    /**ottieni messsaggi da backEnd, costruisce mappa per ordinare visualizzazione anteprima MessageList*/
    fun getMessages() {
        viewModelScope.launch {
            val map: MutableMap<Pair<String, String>, MutableList<Message>> = mutableMapOf()

            uiStateMessage = try {
                val userMessageList = Service.retrofitService.getMessages(1)//TODO:sosistuire con utenteLoggato
               /**ragruppa messaggi stessa conversazione*/
                for(userMessage  in userMessageList)
                {
                    /**costruisce chiave  1,2 = 2,1 (stessa Conversazione)*/
                    val key: Pair<String, String> =
                        if (userMessage.mittenteNome == "Paperino") //TODO:sosistuire con utenteLoggato
                            Pair(userMessage.mittenteNome, userMessage.destinatarioNome)
                        else
                            Pair(userMessage.destinatarioNome, userMessage.mittenteNome)
                    /**crea conversazione se non mappata, altrimenti aggiunge a conversazione gia mappata*/
                    val listaConversazione: MutableList<Message> = map[key] ?: mutableListOf()
                    listaConversazione.add(userMessage)
                    map[key] = listaConversazione

                }
                UiStateMessage.Success(map)//anteprima messaggi raggruppati per conversazione
            } catch (e: IOException) { UiStateMessage.Error }
        }
    }

    /**@Post backEnd save - TODO:inserire mittenteId=utenteLoggato */
    fun sendMessage(message: String, venditoreId: Long) {
        val m=Message(testo=message, mittenteId = 1, destinatarioId = venditoreId)
        viewModelScope.launch {
            uiStateSendMessage = try {
                Service.retrofitService.saveMessage(m)
                UiStateSendMessage.Success
            } catch (e: IOException) { UiStateSendMessage.Error }
        }
    }


    /**ricontatta backEnd per ottenere messaggi*/
    fun setLoadingMessageState() { uiStateMessage=UiStateMessage.Loading}
    fun setLoadingSendMessageState() { uiStateSendMessage=UiStateSendMessage.Loading }


}