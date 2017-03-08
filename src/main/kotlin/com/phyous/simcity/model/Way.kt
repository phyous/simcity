package com.phyous.simcity.model

import java.util.*

/**
 * A 'way' in openstreetmap is an ordered list of nodes which normally also has at least one tag or is included
 * within a Relation. A way can have between 2 and 2,000 nodes, although it's possible that faulty ways with zero or
 * a single node exist. A way can be open or closed. A closed way is one whose last node on the way is also the first
 * on that way. A closed way may be interpreted either as a closed polyline, or an area, or both.
 */
data class Way(val id: Long, val nodes: List<Long>, val tags: Map<String, String> = HashMap())