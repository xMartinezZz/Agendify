package com.agendify.domain.repositories;

import com.agendify.domain.entities.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.cliente.id = :id OR a.estabelecimento.id = :id " +
            "AND a.status = com.agendify.domain.entities.Status.AGENDADO")
    List<Agendamento> findAgendamentoByUserIdOrEstabelecimentoId(@Param("id") UUID id);

    //TODO: Provavelmente esse será o metodo usado na pesquisa de agendamento
    @Query("SELECT a FROM Agendamento a " +
            "WHERE (a.cliente.id = :id OR a.estabelecimento.id = :id) " +
            "AND FUNCTION('TRUNC', a.data, 'MM') = FUNCTION('TRUNC', :data, 'MM') " +
            "AND FUNCTION('TRUNC', a.data, 'YYYY') = FUNCTION('TRUNC', :data, 'YYYY') " +
            "AND a.status = com.agendify.domain.entities.Status.AGENDADO")
    List<Agendamento> findAgendamentoByUserIdOrEstabelecimentoIdAndMonthYear(@Param("id") UUID id,
                                                                             @Param("data") Date data);
    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.data BETWEEN :horaInicio AND FUNCTION('DATE_ADD', a.data, FUNCTION('INTERVAL', a.servico.duracao, 'MINUTE')) " +
            "AND a.estabelecimento.id = :estabelecimentoId " +
            "AND a.status = com.agendify.domain.entities.Status.AGENDADO")
    List<Agendamento> findAgendamentosDisponiveis(
            @Param("horaInicio") Date horaInicio,
            @Param("estabelecimentoId") UUID estabelecimentoId
    );
}
