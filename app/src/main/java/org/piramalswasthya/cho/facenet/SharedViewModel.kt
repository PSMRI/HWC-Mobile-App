package org.piramalswasthya.cho.facenet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _photoUri = MutableLiveData<String>()
    val photoUri: LiveData<String> = _photoUri

    private val _faceVector = MutableLiveData<FloatArray>()
    val faceVector: LiveData<FloatArray> = _faceVector

    fun setPhotoUri(uri: String) {
        _photoUri.value = uri
    }

    fun setFaceVector(faceVector: FloatArray) {
        _faceVector.value = faceVector
    }
}