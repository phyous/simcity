package com.phyous.simcity.model

data class Edge(val id: Long, val nodes: List<Long>, val tags: Map<String, String>)