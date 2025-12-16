package com.delivery.setting.model

data class ReorderItem(
    val id: String,
    val fromLocation: String,
    val fromAddress: String,
    val toLocation: String,
    val toAddress: String,
    val packageType: String,
    val packageWeight: String,
    val orderDate: String,
    val status: String,
    val price: String? = null
)
