package io.newm.shared.internal.implementations

import com.liftric.kvault.KVault
import io.newm.shared.internal.db.PreferencesDataStore

class PreferencesDataStoreImpl : PreferencesDataStore {
    private val vault: KVault = KVault("newm_ios_preferences")

    override fun saveString(key: String, value: String) {
        vault.set(key, value)
    }

    override fun getString(key: String): String? {
        return vault.string(key)
    }

    override fun saveInt(key: String, value: Int) {
        vault.set(key, value)
    }

    override fun getInt(key: String): Int? {
        return vault.int(key)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        vault.set(key, value)
    }

    override fun getBoolean(key: String): Boolean? {
        return vault.bool(key)
    }

    override fun deleteValue(key: String) {
        vault.deleteObject(key)
    }

    override fun clearAll() {
        vault.clear()
    }
}