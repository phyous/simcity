package com.phyous.simcity.model

data class Node(val id: Long, val lat: Double, val lng: Double) {
    override fun toString(): String {
        return String.format("id[%d] lat[%.4f] lng[%.4f]", this.id, this.lat, this.lng)
    }
}
