package com.project.core.util

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import org.springframework.data.domain.Pageable

class QueryDslUtil {
    companion object {
        fun orders(
            pageable: Pageable,
            classType: Class<*>,
            name: String,
        ): Array<OrderSpecifier<*>> {
            val orders: MutableList<OrderSpecifier<*>> = mutableListOf()
            if (!pageable.sort.isEmpty) {
                pageable.sort.forEach {
                    val direction = if (it.isAscending) Order.ASC else Order.DESC
                    val orderByExpression: PathBuilder<*> = PathBuilder(classType, name)
                    orders.add(OrderSpecifier(direction, orderByExpression[it.property] as Expression<Comparable<*>>))
                }
            }

            return orders.toTypedArray()
        }
    }
}
