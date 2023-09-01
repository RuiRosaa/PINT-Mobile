package com.hardtinsa

import java.time.LocalDateTime

class Calutils {
    data class DadosAtividade(
        val idoportunidade: Int,
        val tipoativ: String,
        val estado: Int,
        val assunto: String,
        val detalhe: String,
        val startTime: LocalDateTime?,
        val endTime: LocalDateTime?
    )

    data class DadosEntrevista(
        val data_hora: LocalDateTime?,
        val data_hora_fim: LocalDateTime?,
        val estado: Int
    )

    data class DadosEntrevistaTecnicas(
        val identrevista: Int,
        val idcandidatura: Int,
        val nc: String,
        val data_hora: LocalDateTime?,
        val data_hora_fim: LocalDateTime?,
        val estado: Int,
        val comentarios: String,
        val classificacao: String

    )

    companion object {
        fun getIdAtividadeFromListItem(item: String): Int {
            val pattern = "Tipo de Atividade:(\\d+)".toRegex()
            val matchResult = pattern.find(item)
            val idatividade = matchResult?.groupValues?.get(1)?.toIntOrNull()

            return idatividade ?: -1 // Retorna -1 caso o ID da atividade não possa ser obtido
        }

        fun getIdEntrevistaFromListItem(item: String): Int {
            val pattern = "Entrevista: (\\d+)".toRegex()
            val matchResult = pattern.find(item)
            val idEntrevista = matchResult?.groupValues?.get(1)?.toIntOrNull()

            return idEntrevista ?: -1 // Retorna -1 caso o ID da entrevista não possa ser obtido
        }

    }
}