package com.cirida.zhao.linxian.leader.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cirida.zhao.linxian.api.leader.dto.PickupPointDTO;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.leader.dto.LeaderRequest;
import com.cirida.zhao.linxian.leader.dto.PickupPointRequest;
import com.cirida.zhao.linxian.leader.entity.Leader;
import com.cirida.zhao.linxian.leader.entity.PickupPoint;
import com.cirida.zhao.linxian.leader.mapper.LeaderMapper;
import com.cirida.zhao.linxian.leader.mapper.PickupPointMapper;
import com.cirida.zhao.linxian.leader.service.LeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 团长与自提点管理。{@link #getPickupPoint} 同时是订单服务 Feign 调用的实现入口。
 */
@Service
@RequiredArgsConstructor
public class LeaderServiceImpl implements LeaderService {

    private final LeaderMapper leaderMapper;
    private final PickupPointMapper pickupPointMapper;

    @Override
    public List<Leader> listLeaders() {
        return leaderMapper.selectList(new LambdaQueryWrapper<Leader>().orderByDesc(Leader::getId));
    }

    @Override
    public Leader createLeader(LeaderRequest request) {
        Leader leader = new Leader();
        leader.setName(request.getName());
        leader.setPhone(request.getPhone());
        leader.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        leaderMapper.insert(leader);
        return leader;
    }

    @Override
    public Leader updateLeader(Long id, LeaderRequest request) {
        Leader leader = leaderMapper.selectById(id);
        if (leader == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        leader.setName(request.getName());
        leader.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            leader.setStatus(request.getStatus());
        }
        leaderMapper.updateById(leader);
        return leader;
    }

    @Override
    public void deleteLeader(Long id) {
        leaderMapper.deleteById(id);
    }

    @Override
    public PickupPointDTO getPickupPoint(Long id) {
        PickupPoint point = pickupPointMapper.selectById(id);
        if (point == null) {
            throw new BizException(ErrorCode.PICKUP_POINT_NOT_FOUND);
        }
        return toDTO(point);
    }

    @Override
    public List<PickupPoint> listPickupPoints(Long leaderId) {
        return pickupPointMapper.selectList(new LambdaQueryWrapper<PickupPoint>()
                .eq(leaderId != null, PickupPoint::getLeaderId, leaderId)
                .orderByDesc(PickupPoint::getId));
    }

    @Override
    public PickupPoint createPickupPoint(PickupPointRequest request) {
        PickupPoint point = new PickupPoint();
        point.setLeaderId(request.getLeaderId());
        point.setName(request.getName());
        point.setAddress(request.getAddress());
        point.setPhone(request.getPhone());
        point.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        pickupPointMapper.insert(point);
        return point;
    }

    @Override
    public PickupPoint updatePickupPoint(Long id, PickupPointRequest request) {
        PickupPoint point = pickupPointMapper.selectById(id);
        if (point == null) {
            throw new BizException(ErrorCode.PICKUP_POINT_NOT_FOUND);
        }
        point.setLeaderId(request.getLeaderId());
        point.setName(request.getName());
        point.setAddress(request.getAddress());
        point.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            point.setStatus(request.getStatus());
        }
        pickupPointMapper.updateById(point);
        return point;
    }

    @Override
    public void deletePickupPoint(Long id) {
        pickupPointMapper.deleteById(id);
    }

    private PickupPointDTO toDTO(PickupPoint point) {
        PickupPointDTO dto = new PickupPointDTO();
        dto.setId(point.getId());
        dto.setLeaderId(point.getLeaderId());
        dto.setName(point.getName());
        dto.setAddress(point.getAddress());
        dto.setPhone(point.getPhone());
        return dto;
    }
}
