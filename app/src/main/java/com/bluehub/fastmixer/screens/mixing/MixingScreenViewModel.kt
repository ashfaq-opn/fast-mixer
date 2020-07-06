package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import javax.inject.Inject

class MixingScreenViewModel(override val context: Context?, override val tag: String): PermissionViewModel(context, tag) {
    init {
        getViewModelComponent().inject(this)
    }

    @Inject
    override lateinit var permissionManager: PermissionManager

    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    val _eventRead = MutableLiveData<Boolean>()
        val eventRead: LiveData<Boolean>
        get() = _eventRead

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onReadFromDisk() {

    }

    fun onSaveToDisk() {

    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }
}