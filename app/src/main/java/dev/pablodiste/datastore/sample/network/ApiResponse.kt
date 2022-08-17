package dev.pablodiste.datastore.sample.network

import com.google.gson.annotations.SerializedName

data class ApiListResponse<T>(@SerializedName("results") val results: List<T>)
