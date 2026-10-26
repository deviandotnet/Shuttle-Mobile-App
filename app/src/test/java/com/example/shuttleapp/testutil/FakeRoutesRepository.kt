package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.Route
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeRoutesRepository(
    private val names: Map<String, String> = emptyMap(),
    private val routes: List<Route> = emptyList()
) : RoutesRepository {

    override suspend fun getRouteNameById(id: String): String =
        names[id] ?: throw NoSuchElementException("No route $id")

    override suspend fun getNameByRouteId(id: String): Flow<String> = flowOf(getRouteNameById(id))
    override suspend fun getAllRoutes(forceFetchFromRemote: Boolean): Flow<Resource<List<Route>>> =
        if (routes.isEmpty()) emptyFlow() else flowOf(Resource.Success(routes))
    override suspend fun getRouteById(id: String): Flow<Resource<Route>> = emptyFlow()
    override suspend fun getRouteIdByName(routeName: String): String =
        routes.firstOrNull { it.routename == routeName }?.routeid ?: ""
}
