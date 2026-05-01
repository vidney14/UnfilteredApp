package com.example.unfilteredapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unfilteredapp.data.api.NetworkConstants
import com.example.unfilteredapp.data.model.Message
import com.example.unfilteredapp.data.model.Room
import com.example.unfilteredapp.data.repository.ChatRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var socket: Socket? = null
    private var currentRoomId: Int? = null

    init {
        setupSocket()
        fetchRooms()
    }

    private fun setupSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("polling", "websocket") 
                reconnection = true
                forceNew = true
            }
            
            socket = IO.socket(NetworkConstants.SOCKET_URL, options)
            
            socket?.on(Socket.EVENT_CONNECT) {
                _isConnected.value = true
                _error.value = null
                Log.d(TAG, "Socket connected")
            }

            socket?.on("receive_message") { args ->
                val data = args[0] as JSONObject
                Log.d(TAG, "Received message: ${data.optString("content", "")}")
                viewModelScope.launch {
                    val incoming = Message(
                        id = data.optInt("id"),
                        room_id = if (data.has("room_id")) data.getInt("room_id") else data.optInt("roomId"),
                        user_id_snake = if (data.has("user_id")) data.getInt("user_id") else null,
                        user_id_camel = if (data.has("userId")) data.getInt("userId") else null,
                        user_name_snake = if (data.has("user_name")) data.getString("user_name") else null,
                        user_name_camel = if (data.has("userName")) data.getString("userName") else null,
                        content = data.optString("content", ""),
                        created_at = data.optString("created_at")
                    )
                    
                    if (incoming.effectiveRoomId == currentRoomId || incoming.room_id == currentRoomId) {
                        val currentList = _messages.value.toMutableList()
                        
                        // DUPLICATE/REPLACEMENT LOGIC
                        // We look for a local message (id is null) that matches the content and user
                        val optimisticIndex = currentList.indexOfFirst { 
                            it.id == null && it.content == incoming.content && it.effectiveUserId == incoming.effectiveUserId
                        }

                        if (optimisticIndex != -1) {
                            // Perfect! The server confirmed our message. Replace the temp one.
                            currentList[optimisticIndex] = incoming
                        } else {
                            // Check if this specific message ID is already in our list (avoid double broadcasts)
                            val alreadyExists = currentList.any { it.id == incoming.id && it.id != 0 }
                            if (!alreadyExists) {
                                currentList.add(incoming)
                            }
                        }
                        _messages.value = currentList
                    }
                }
            }
            
            socket?.connect()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun fetchRooms() {
    viewModelScope.launch {
        runCatching {
            repository.getRooms()
        }.onSuccess { result ->
            if (result.isSuccessful) {
                _rooms.value = result.body().orEmpty()
            }
        }.onFailure { throwable ->
            _error.value = "Failed to load rooms: ${throwable.message}"
        }
    }
}

    fun joinRoom(roomId: Int) {
    currentRoomId = roomId

    _messages.value = emptyList<Message>().also {
        socket?.emit("join_room", roomId)
    }

    fetchMessages(roomId)
}

    private fun fetchMessages(roomId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getMessages(roomId)
                if (response.isSuccessful) {
                    _messages.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(roomId: Int, userId: Int, userName: String, content: String) {
        val messageObject = JSONObject().apply {
            put("room_id", roomId)
            put("user_id", userId)
            put("user_name", userName)
            put("content", content)
        }
        
        // Optimistic UI update with id=null to mark it as local
        val tempMessage = Message(
            id = null,
            room_id = roomId,
            user_id_snake = userId,
            user_name_snake = userName,
            content = content,
            created_at = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
        _messages.value = _messages.value + tempMessage
        
        socket?.emit("send_message", messageObject)
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
