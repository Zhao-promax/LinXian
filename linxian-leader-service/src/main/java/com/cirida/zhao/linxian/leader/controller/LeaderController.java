package com.cirida.zhao.linxian.leader.controller;

import com.cirida.zhao.linxian.api.leader.dto.PickupPointDTO;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.leader.dto.LeaderRequest;
import com.cirida.zhao.linxian.leader.dto.PickupPointRequest;
import com.cirida.zhao.linxian.leader.entity.Leader;
import com.cirida.zhao.linxian.leader.entity.PickupPoint;
import com.cirida.zhao.linxian.leader.service.LeaderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 团长/自提点接口。网关路由 {@code /leader/**}，本服务映射保持相同前缀。
 */
@RestController
@RequestMapping("/leader")
@RequiredArgsConstructor
public class LeaderController {

    private final LeaderService leaderService;

    @GetMapping("/list")
    public Result<List<Leader>> listLeaders() {
        return Result.ok(leaderService.listLeaders());
    }

    @PostMapping
    public Result<Leader> createLeader(@Valid @RequestBody LeaderRequest request) {
        return Result.ok(leaderService.createLeader(request));
    }

    @PutMapping("/{id}")
    public Result<Leader> updateLeader(@PathVariable Long id, @Valid @RequestBody LeaderRequest request) {
        return Result.ok(leaderService.updateLeader(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteLeader(@PathVariable Long id) {
        leaderService.deleteLeader(id);
        return Result.ok();
    }

    @GetMapping("/pickup-points/{id}")
    public Result<PickupPointDTO> getPickupPoint(@PathVariable Long id) {
        return Result.ok(leaderService.getPickupPoint(id));
    }

    @GetMapping("/pickup-points")
    public Result<List<PickupPoint>> listPickupPoints(@RequestParam(required = false) Long leaderId) {
        return Result.ok(leaderService.listPickupPoints(leaderId));
    }

    @PostMapping("/pickup-points")
    public Result<PickupPoint> createPickupPoint(@Valid @RequestBody PickupPointRequest request) {
        return Result.ok(leaderService.createPickupPoint(request));
    }

    @PutMapping("/pickup-points/{id}")
    public Result<PickupPoint> updatePickupPoint(@PathVariable Long id, @Valid @RequestBody PickupPointRequest request) {
        return Result.ok(leaderService.updatePickupPoint(id, request));
    }

    @DeleteMapping("/pickup-points/{id}")
    public Result<Void> deletePickupPoint(@PathVariable Long id) {
        leaderService.deletePickupPoint(id);
        return Result.ok();
    }
}
