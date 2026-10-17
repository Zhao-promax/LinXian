package com.cirida.zhao.linxian.fulfillment.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cirida.zhao.linxian.fulfillment.dto.FulfillmentExceptionDoc;
import com.cirida.zhao.linxian.fulfillment.entity.FulfillmentException;
import com.cirida.zhao.linxian.fulfillment.mapper.FulfillmentExceptionMapper;
import com.cirida.zhao.linxian.fulfillment.service.FulfillmentExceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 履约异常：MySQL 为准，ES 冗余检索（先落库后写 ES，写 ES 失败仅告警不影响主流程）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentExceptionServiceImpl implements FulfillmentExceptionService {

    private static final String ES_INDEX = "fulfillment_exception";

    private final FulfillmentExceptionMapper exceptionMapper;
    private final ElasticsearchClient esClient;

    @Override
    public void record(Long orderId, String orderNo, Long skuId, Long leaderId, Long pickupPointId,
                       String type, String description) {
        FulfillmentException exception = new FulfillmentException();
        exception.setOrderId(orderId);
        exception.setOrderNo(orderNo);
        exception.setSkuId(skuId);
        exception.setLeaderId(leaderId);
        exception.setPickupPointId(pickupPointId);
        exception.setExceptionType(type);
        exception.setDescription(description);
        exception.setStatus(0);
        exception.setRetryCount(0);
        exception.setNextRetryTime(LocalDateTime.now().plusSeconds(30));
        exceptionMapper.insert(exception);

        indexToEs(toDoc(exception));
    }

    @Override
    public List<FulfillmentExceptionDoc> search(Long leaderId, Long skuId, String orderNo, Long from, Long to) {
        try {
            SearchResponse<FulfillmentExceptionDoc> response = esClient.search(s -> s
                            .index(ES_INDEX)
                            .query(buildQuery(leaderId, skuId, orderNo, from, to)),
                    FulfillmentExceptionDoc.class);
            List<FulfillmentExceptionDoc> docs = new ArrayList<>();
            for (Hit<FulfillmentExceptionDoc> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    docs.add(hit.source());
                }
            }
            return docs;
        } catch (Exception e) {
            log.warn("ES 检索失败，降级为数据库查询", e);
            return searchFromDb(leaderId, skuId, orderNo, from, to);
        }
    }

    @Override
    public List<FulfillmentException> listPending() {
        return exceptionMapper.selectList(new LambdaQueryWrapper<FulfillmentException>()
                .eq(FulfillmentException::getStatus, 0)
                .le(FulfillmentException::getNextRetryTime, LocalDateTime.now()));
    }

    @Override
    public void retry(FulfillmentException exception) {
        exception.setRetryCount(exception.getRetryCount() + 1);
        exception.setStatus(1); // 已补偿
        exception.setNextRetryTime(null);
        exceptionMapper.updateById(exception);
        indexToEs(toDoc(exception));
    }

    private Query buildQuery(Long leaderId, Long skuId, String orderNo, Long from, Long to) {
        List<Query> must = new ArrayList<>();
        if (leaderId != null) {
            must.add(Query.of(q -> q.term(t -> t.field("leaderId").value(FieldValue.of(leaderId)))));
        }
        if (skuId != null) {
            must.add(Query.of(q -> q.term(t -> t.field("skuId").value(FieldValue.of(skuId)))));
        }
        if (orderNo != null && !orderNo.isBlank()) {
            must.add(Query.of(q -> q.term(t -> t.field("orderNo").value(FieldValue.of(orderNo)))));
        }
        if (from != null || to != null) {
            must.add(Query.of(q -> q.range(r -> r.longNumber(l -> {
                l.field("createdAt");
                if (from != null) {
                    l.gte(from);
                }
                if (to != null) {
                    l.lte(to);
                }
                return l;
            }))));
        }
        if (must.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        return Query.of(q -> q.bool(b -> b.must(must)));
    }

    private List<FulfillmentExceptionDoc> searchFromDb(Long leaderId, Long skuId, String orderNo, Long from, Long to) {
        LambdaQueryWrapper<FulfillmentException> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(leaderId != null, FulfillmentException::getLeaderId, leaderId)
                .eq(skuId != null, FulfillmentException::getSkuId, skuId)
                .eq(orderNo != null && !orderNo.isBlank(), FulfillmentException::getOrderNo, orderNo);
        if (from != null) {
            wrapper.ge(FulfillmentException::getCreatedAt, toLocalDateTime(from));
        }
        if (to != null) {
            wrapper.le(FulfillmentException::getCreatedAt, toLocalDateTime(to));
        }
        return exceptionMapper.selectList(wrapper).stream().map(this::toDoc).toList();
    }

    private void indexToEs(FulfillmentExceptionDoc doc) {
        try {
            esClient.index(i -> i.index(ES_INDEX).id(String.valueOf(doc.getId())).document(doc));
        } catch (Exception e) {
            log.warn("履约异常同步 ES 失败（以 MySQL 为准，不影响主流程）id={}", doc.getId(), e);
        }
    }

    private FulfillmentExceptionDoc toDoc(FulfillmentException e) {
        FulfillmentExceptionDoc doc = new FulfillmentExceptionDoc();
        doc.setId(e.getId());
        doc.setOrderId(e.getOrderId());
        doc.setOrderNo(e.getOrderNo());
        doc.setSkuId(e.getSkuId());
        doc.setLeaderId(e.getLeaderId());
        doc.setPickupPointId(e.getPickupPointId());
        doc.setExceptionType(e.getExceptionType());
        doc.setDescription(e.getDescription());
        doc.setStatus(e.getStatus());
        doc.setRetryCount(e.getRetryCount());
        doc.setCreatedAt(e.getCreatedAt() == null ? System.currentTimeMillis()
                : e.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return doc;
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
