package com.delivery.core.adapter

class ModelWrapper(var model: Any?, var viewType: Int) : Cloneable {
    var id = 0
    var isSelected = false

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ModelWrapper {
        return super.clone() as ModelWrapper
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelWrapper

        if (model != other.model) return false
        if (viewType != other.viewType) return false
        if (id != other.id) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model?.hashCode() ?: 0
        result = 31 * result + viewType
        result = 31 * result + id
        result = 31 * result + isSelected.hashCode()
        return result
    }

}