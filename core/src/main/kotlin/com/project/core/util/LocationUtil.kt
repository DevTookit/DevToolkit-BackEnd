package com.project.core.util

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.stereotype.Component

@Component
class LocationUtil {
    companion object {
        fun createPoint(
            latitude: Double,
            longitude: Double,
        ): Point {
            Coordinate(longitude, latitude).let {
                val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
                return geometryFactory.createPoint(it)
            }
        }
    }
}
