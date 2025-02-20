package fr.livio.gateway

@JvmRecord
data class GatewayRouteConfiguration(val gatewayRoutes: LinkedHashSet<GatewayRouteConfigurationElement>)

@JvmRecord
data class GatewayRouteConfigurationElement(val method: String, val serviceOut: String, val pathIn: String, val pathOut: String)