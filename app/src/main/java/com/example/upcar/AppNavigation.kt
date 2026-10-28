package com.example.upcar

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Rotas {
    const val BOAS_VINDAS = "boasVindas"
    const val CADASTRO = "cadastro"
    const val MOTORISTAS = "motoristas"
    const val CHAT = "chat"
    const val EM_ANDAMENTO = "emAndamento"
    const val PERFIL_MOTORISTA = "perfilMotorista"
    const val CORRIDA_FINALIZADA = "corridaFinalizada"

    const val ARG_CARONA_ID = "caronaId"
    const val ARG_MOTORISTA_ID = "motoristaId"

    const val CHAT_ROTA = "$CHAT/{$ARG_CARONA_ID}"
    const val EM_ANDAMENTO_ROTA = "$EM_ANDAMENTO/{$ARG_CARONA_ID}"
    const val PERFIL_MOTORISTA_ROTA = "$PERFIL_MOTORISTA/{$ARG_MOTORISTA_ID}"
    const val CORRIDA_FINALIZADA_ROTA = "$CORRIDA_FINALIZADA/{$ARG_CARONA_ID}"

    fun chat(caronaId: Int) = "$CHAT/$caronaId"
    fun emAndamento(caronaId: Int) = "$EM_ANDAMENTO/$caronaId"
    fun perfilMotorista(motoristaId: Int) = "$PERFIL_MOTORISTA/$motoristaId"
    fun corridaFinalizada(caronaId: Int) = "$CORRIDA_FINALIZADA/$caronaId"
}

@Composable
fun AppNavigation(state: CaronaViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rotas.BOAS_VINDAS
    ) {
        composable(Rotas.BOAS_VINDAS) {
            BoasVindas(onEntrar = { navController.navigate(Rotas.CADASTRO) })
        }

        composable(Rotas.CADASTRO) {
            CadastroPassageiro(
                state = state,
                onVoltar = { navController.popBackStack() },
                onConcluir = {
                    navController.navigate(Rotas.MOTORISTAS) {
                        popUpTo(Rotas.BOAS_VINDAS) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.MOTORISTAS) {
            MotoristasScreen(
                state = state,
                onSolicitar = { motorista ->
                    val carona = state.solicitarCarona(motorista)
                    navController.navigate(Rotas.chat(carona.id)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Rotas.CHAT_ROTA,
            arguments = listOf(navArgument(Rotas.ARG_CARONA_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val caronaId = backStackEntry.arguments?.getInt(Rotas.ARG_CARONA_ID) ?: -1
            Chat(
                state = state,
                caronaId = caronaId,
                onVoltar = { navController.popBackStack() },
                onConfirmar = {
                    navController.navigate(Rotas.emAndamento(caronaId)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Rotas.EM_ANDAMENTO_ROTA,
            arguments = listOf(navArgument(Rotas.ARG_CARONA_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val caronaId = backStackEntry.arguments?.getInt(Rotas.ARG_CARONA_ID) ?: -1
            CorridaEmAndamento(
                state = state,
                caronaId = caronaId,
                onFinalizar = {
                    state.finalizarCorrida(caronaId)
                    navController.navigate(Rotas.corridaFinalizada(caronaId)) {
                        popUpTo(Rotas.EM_ANDAMENTO_ROTA) { inclusive = true }
                    }
                },
                onVerPerfil = { id ->
                    navController.navigate(Rotas.perfilMotorista(id)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Rotas.PERFIL_MOTORISTA_ROTA,
            arguments = listOf(navArgument(Rotas.ARG_MOTORISTA_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val motoristaId = backStackEntry.arguments?.getInt(Rotas.ARG_MOTORISTA_ID) ?: -1
            PerfilMotorista(
                state = state,
                motoristaId = motoristaId,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable(
            route = Rotas.CORRIDA_FINALIZADA_ROTA,
            arguments = listOf(navArgument(Rotas.ARG_CARONA_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val caronaId = backStackEntry.arguments?.getInt(Rotas.ARG_CARONA_ID) ?: -1
            CorridaFinalizada(
                state = state,
                caronaId = caronaId,
                onConcluir = { navController.popBackStack(Rotas.MOTORISTAS, false) }
            )
        }
    }
}
