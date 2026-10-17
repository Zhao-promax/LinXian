package com.cirida.zhao.linxian.leader.service;

import com.cirida.zhao.linxian.api.leader.dto.PickupPointDTO;
import com.cirida.zhao.linxian.leader.dto.LeaderRequest;
import com.cirida.zhao.linxian.leader.dto.PickupPointRequest;
import com.cirida.zhao.linxian.leader.entity.Leader;
import com.cirida.zhao.linxian.leader.entity.PickupPoint;

import java.util.List;

public interface LeaderService {

    List<Leader> listLeaders();

    Leader createLeader(LeaderRequest request);

    Leader updateLeader(Long id, LeaderRequest request);

    void deleteLeader(Long id);

    PickupPointDTO getPickupPoint(Long id);

    List<PickupPoint> listPickupPoints(Long leaderId);

    PickupPoint createPickupPoint(PickupPointRequest request);

    PickupPoint updatePickupPoint(Long id, PickupPointRequest request);

    void deletePickupPoint(Long id);
}
